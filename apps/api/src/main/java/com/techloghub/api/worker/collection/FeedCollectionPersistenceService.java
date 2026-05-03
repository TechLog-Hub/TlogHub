package com.techloghub.api.worker.collection;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.techloghub.api.common.util.StringNormalizer;
import com.techloghub.api.content.domain.ArchivedPost;
import com.techloghub.api.content.domain.CollectionRun;
import com.techloghub.api.content.domain.DuplicateType;
import com.techloghub.api.content.domain.PostSourceOccurrence;
import com.techloghub.api.content.domain.SourceBlog;
import com.techloghub.api.content.repository.ArchivedPostRepository;
import com.techloghub.api.content.repository.CollectionRunRepository;
import com.techloghub.api.content.repository.PostSourceOccurrenceRepository;
import com.techloghub.api.content.repository.SourceBlogRepository;
import com.techloghub.api.worker.normalization.CanonicalFingerprintGenerator;
import com.techloghub.api.worker.normalization.PostSlugGenerator;

import lombok.RequiredArgsConstructor;

/**
 * 수집 source별 DB 변경을 독립 트랜잭션으로 저장한다.
 */
@Service
@RequiredArgsConstructor
public class FeedCollectionPersistenceService {

	private static final int FAILURE_REASON_MAX_LENGTH = 1_000;

	private final SourceBlogRepository sourceBlogRepository;
	private final ArchivedPostRepository archivedPostRepository;
	private final PostSourceOccurrenceRepository postSourceOccurrenceRepository;
	private final CollectionRunRepository collectionRunRepository;
	private final CanonicalFingerprintGenerator canonicalFingerprintGenerator;
	private final PostSlugGenerator postSlugGenerator;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public FeedCollectionTarget prepare(Long sourceBlogId, Instant startedAt) {
		SourceBlog sourceBlog = getSourceBlog(sourceBlogId);
		CollectionRun collectionRun = collectionRunRepository.save(CollectionRun.start(sourceBlog, startedAt));
		return new FeedCollectionTarget(sourceBlog.getId(), collectionRun.getId(), sourceBlog.getFeedUrl());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public FeedCollectionResult recordSuccess(
		FeedCollectionTarget target,
		List<FeedEntryCandidate> candidates,
		Instant finishedAt
	) {
		SourceBlog sourceBlog = getSourceBlog(target.sourceBlogId());
		CollectionRun collectionRun = getCollectionRun(target.collectionRunId());

		int collectedCount = 0;
		int newPostCount = 0;
		int duplicateCount = 0;
		int skippedCount = 0;

		for (FeedEntryCandidate candidate : candidates) {
			try {
				String fingerprint = canonicalFingerprintGenerator.generate(candidate.canonicalUrl());
				ArchivedPost archivedPost = archivedPostRepository.findByCanonicalFingerprint(fingerprint)
					.orElse(null);
				collectedCount++;

				if (archivedPost == null) {
					archivedPost = createArchivedPost(sourceBlog, candidate, fingerprint, finishedAt);
					newPostCount++;
					saveOccurrenceIfAbsent(archivedPost, sourceBlog, candidate, DuplicateType.ORIGINAL);
					continue;
				}

				duplicateCount++;
				saveOccurrenceIfAbsent(archivedPost, sourceBlog, candidate, DuplicateType.EXACT_DUPLICATE);
			} catch (IllegalArgumentException exception) {
				skippedCount++;
			}
		}

		sourceBlog.markCollectedAt(finishedAt);
		collectionRun.succeed(finishedAt, collectedCount, newPostCount);
		return FeedCollectionResult.success(
			sourceBlog.getId(),
			collectedCount,
			newPostCount,
			duplicateCount,
			skippedCount
		);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public FeedCollectionResult recordFailure(FeedCollectionTarget target, Throwable exception, Instant finishedAt) {
		CollectionRun collectionRun = getCollectionRun(target.collectionRunId());
		collectionRun.fail(finishedAt, formatFailureReason(exception));
		return FeedCollectionResult.failure(target.sourceBlogId(), formatFailureReason(exception));
	}

	private ArchivedPost createArchivedPost(
		SourceBlog sourceBlog,
		FeedEntryCandidate candidate,
		String fingerprint,
		Instant collectedAt
	) {
		String slug = postSlugGenerator.generate(sourceBlog, candidate.title(), archivedPostRepository::existsBySlug);
		ArchivedPost archivedPost = ArchivedPost.collect(
			sourceBlog,
			slug,
			candidate.title(),
			candidate.originUrl(),
			candidate.canonicalUrl(),
			fingerprint,
			candidate.publishedAt(),
			collectedAt
		);
		return archivedPostRepository.save(archivedPost);
	}

	private void saveOccurrenceIfAbsent(
		ArchivedPost archivedPost,
		SourceBlog sourceBlog,
		FeedEntryCandidate candidate,
		DuplicateType duplicateType
	) {
		if (postSourceOccurrenceRepository.existsBySourceBlog_IdAndOriginUrl(sourceBlog.getId(), candidate.originUrl())) {
			return;
		}
		PostSourceOccurrence occurrence = duplicateType == DuplicateType.ORIGINAL
			? PostSourceOccurrence.original(archivedPost, sourceBlog, candidate.originUrl(), candidate.publishedAt())
			: PostSourceOccurrence.duplicate(archivedPost, sourceBlog, candidate.originUrl(), candidate.publishedAt(), duplicateType);
		postSourceOccurrenceRepository.save(occurrence);
	}

	private SourceBlog getSourceBlog(Long sourceBlogId) {
		return sourceBlogRepository.findWithCompanyById(sourceBlogId)
			.orElseThrow(() -> new IllegalArgumentException("sourceBlog not found: " + sourceBlogId));
	}

	private CollectionRun getCollectionRun(Long collectionRunId) {
		return collectionRunRepository.findById(collectionRunId)
			.orElseThrow(() -> new IllegalArgumentException("collectionRun not found: " + collectionRunId));
	}

	private static String formatFailureReason(Throwable exception) {
		String message = exception.getClass().getSimpleName() + ": " + exception.getMessage();
		return StringNormalizer.truncate(message, FAILURE_REASON_MAX_LENGTH);
	}
}

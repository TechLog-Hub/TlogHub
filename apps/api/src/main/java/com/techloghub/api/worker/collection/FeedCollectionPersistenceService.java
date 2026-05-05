package com.techloghub.api.worker.collection;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.techloghub.api.common.util.StringNormalizer;
import com.techloghub.api.content.domain.ArchivedPost;
import com.techloghub.api.content.domain.AiSummary;
import com.techloghub.api.content.domain.CollectionRun;
import com.techloghub.api.content.domain.DuplicateType;
import com.techloghub.api.content.domain.FeedEntrySnapshot;
import com.techloghub.api.content.domain.PostSourceOccurrence;
import com.techloghub.api.content.domain.SourceBlog;
import com.techloghub.api.content.repository.AiSummaryRepository;
import com.techloghub.api.content.repository.ArchivedPostRepository;
import com.techloghub.api.content.repository.CollectionRunRepository;
import com.techloghub.api.content.repository.FeedEntrySnapshotRepository;
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
	private final AiSummaryRepository aiSummaryRepository;
	private final FeedEntrySnapshotRepository feedEntrySnapshotRepository;
	private final PostSourceOccurrenceRepository postSourceOccurrenceRepository;
	private final CollectionRunRepository collectionRunRepository;
	private final CanonicalFingerprintGenerator canonicalFingerprintGenerator;
	private final PostSlugGenerator postSlugGenerator;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public FeedCollectionTarget prepare(Long sourceBlogId, Instant startedAt) {
		Objects.requireNonNull(sourceBlogId, "sourceBlogId must not be null");
		Objects.requireNonNull(startedAt, "startedAt must not be null");
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
		Objects.requireNonNull(target, "target must not be null");
		Objects.requireNonNull(candidates, "candidates must not be null");
		Objects.requireNonNull(finishedAt, "finishedAt must not be null");
		SourceBlog sourceBlog = getSourceBlog(target.sourceBlogId());
		CollectionRun collectionRun = getCollectionRun(target.collectionRunId());

		CandidateFingerprintGroup fingerprintGroup = CandidateFingerprintGroup.from(candidates, canonicalFingerprintGenerator);
		Map<String, ArchivedPost> archivedPostsByFingerprint = findArchivedPostsByFingerprint(fingerprintGroup.fingerprints());
		Set<String> existingOriginUrls = findExistingOriginUrls(sourceBlog.getId(), fingerprintGroup.originUrls());

		int collectedCount = fingerprintGroup.candidates().size();
		int newPostCount = 0;
		int duplicateCount = 0;
		int skippedCount = fingerprintGroup.skippedCount();

		for (CandidateFingerprint candidateFingerprint : fingerprintGroup.candidates()) {
			FeedEntryCandidate candidate = candidateFingerprint.candidate();
			String fingerprint = candidateFingerprint.fingerprint();
			ArchivedPost archivedPost = archivedPostsByFingerprint.get(fingerprint);

			if (archivedPost == null) {
				archivedPost = createArchivedPost(sourceBlog, candidate, fingerprint, finishedAt);
				archivedPostsByFingerprint.put(fingerprint, archivedPost);
				newPostCount++;
				saveOccurrenceAndSnapshotIfAbsent(
					archivedPost,
					sourceBlog,
					collectionRun,
					candidate,
					fingerprint,
					DuplicateType.ORIGINAL,
					finishedAt,
					existingOriginUrls
				);
				continue;
			}

			duplicateCount++;
			saveOccurrenceAndSnapshotIfAbsent(
				archivedPost,
				sourceBlog,
				collectionRun,
				candidate,
				fingerprint,
				DuplicateType.EXACT_DUPLICATE,
				finishedAt,
				existingOriginUrls
			);
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
		Objects.requireNonNull(target, "target must not be null");
		Objects.requireNonNull(exception, "exception must not be null");
		Objects.requireNonNull(finishedAt, "finishedAt must not be null");
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
		archivedPost.publish();
		ArchivedPost savedPost = archivedPostRepository.save(archivedPost);
		aiSummaryRepository.save(AiSummary.pending(savedPost, 1));
		return savedPost;
	}

	private void saveOccurrenceAndSnapshotIfAbsent(
		ArchivedPost archivedPost,
		SourceBlog sourceBlog,
		CollectionRun collectionRun,
		FeedEntryCandidate candidate,
		String canonicalFingerprint,
		DuplicateType duplicateType,
		Instant collectedAt,
		Set<String> existingOriginUrls
	) {
		if (existingOriginUrls.contains(candidate.originUrl())) {
			return;
		}
		PostSourceOccurrence occurrence = duplicateType == DuplicateType.ORIGINAL
			? PostSourceOccurrence.original(archivedPost, sourceBlog, candidate.originUrl(), candidate.publishedAt())
			: PostSourceOccurrence.duplicate(archivedPost, sourceBlog, candidate.originUrl(), candidate.publishedAt(), duplicateType);
		postSourceOccurrenceRepository.save(occurrence);
		feedEntrySnapshotRepository.save(FeedEntrySnapshot.create(
			archivedPost,
			sourceBlog,
			collectionRun,
			candidate.title(),
			candidate.originUrl(),
			candidate.canonicalUrl(),
			canonicalFingerprint,
			candidate.publishedAt(),
			collectedAt,
			candidate.summaryText(),
			duplicateType
		));
		existingOriginUrls.add(candidate.originUrl());
	}

	private Map<String, ArchivedPost> findArchivedPostsByFingerprint(Set<String> fingerprints) {
		if (fingerprints.isEmpty()) {
			return new HashMap<>();
		}
		Map<String, ArchivedPost> result = new HashMap<>();
		archivedPostRepository.findByCanonicalFingerprintIn(fingerprints)
			.forEach(archivedPost -> result.put(archivedPost.getCanonicalFingerprint(), archivedPost));
		return result;
	}

	private Set<String> findExistingOriginUrls(Long sourceBlogId, Set<String> originUrls) {
		if (originUrls.isEmpty()) {
			return new HashSet<>();
		}
		Set<String> result = new HashSet<>();
		postSourceOccurrenceRepository.findBySourceBlog_IdAndOriginUrlIn(sourceBlogId, originUrls)
			.forEach(occurrence -> result.add(occurrence.getOriginUrl()));
		return result;
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

	private record CandidateFingerprintGroup(
		List<CandidateFingerprint> candidates,
		Set<String> fingerprints,
		Set<String> originUrls,
		int skippedCount
	) {

		private static CandidateFingerprintGroup from(
			List<FeedEntryCandidate> candidates,
			CanonicalFingerprintGenerator canonicalFingerprintGenerator
		) {
			List<CandidateFingerprint> candidateFingerprints = new java.util.ArrayList<>();
			Set<String> fingerprints = new HashSet<>();
			Set<String> originUrls = new HashSet<>();
			int skippedCount = 0;

			for (FeedEntryCandidate candidate : candidates) {
				try {
					String fingerprint = canonicalFingerprintGenerator.generate(candidate.canonicalUrl());
					candidateFingerprints.add(new CandidateFingerprint(candidate, fingerprint));
					fingerprints.add(fingerprint);
					originUrls.add(candidate.originUrl());
				} catch (IllegalArgumentException exception) {
					skippedCount++;
				}
			}

			return new CandidateFingerprintGroup(candidateFingerprints, fingerprints, originUrls, skippedCount);
		}
	}

	private record CandidateFingerprint(
		FeedEntryCandidate candidate,
		String fingerprint
	) {
	}
}

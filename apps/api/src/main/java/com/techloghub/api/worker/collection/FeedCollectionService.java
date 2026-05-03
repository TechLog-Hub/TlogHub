package com.techloghub.api.worker.collection;

import java.util.List;

import org.springframework.stereotype.Service;

import com.techloghub.api.content.domain.SourceBlog;
import com.techloghub.api.content.domain.SourceBlogStatus;
import com.techloghub.api.content.domain.SourceType;
import com.techloghub.api.content.repository.SourceBlogRepository;

import lombok.RequiredArgsConstructor;

/**
 * 승인된 RSS/Atom source 목록을 순회하며 수집을 orchestration한다.
 */
@Service
@RequiredArgsConstructor
public class FeedCollectionService {

	private final SourceBlogRepository sourceBlogRepository;
	private final FeedSourceCollector feedSourceCollector;

	/**
	 * 승인된 RSS/Atom source 전체를 수집한다.
	 *
	 * @return 전체 수집 요약
	 */
	public FeedCollectionSummary collectApprovedSources() {
		List<FeedCollectionResult> results = sourceBlogRepository.findByStatusOrderByIdAsc(SourceBlogStatus.APPROVED)
			.stream()
			.filter(FeedCollectionService::isCollectable)
			.map(SourceBlog::getId)
			.map(feedSourceCollector::collect)
			.toList();
		return FeedCollectionSummary.from(results);
	}

	/**
	 * 지정한 source 하나를 수집한다.
	 *
	 * @param sourceBlogId source ID
	 * @return source 수집 결과
	 */
	public FeedCollectionResult collectSource(Long sourceBlogId) {
		return sourceBlogRepository.findById(sourceBlogId)
			.filter(sourceBlog -> sourceBlog.getStatus() == SourceBlogStatus.APPROVED)
			.filter(FeedCollectionService::isCollectable)
			.map(sourceBlog -> feedSourceCollector.collect(sourceBlog.getId()))
			.orElseThrow(() -> new IllegalArgumentException("source is not collectable: " + sourceBlogId));
	}

	private static boolean isCollectable(SourceBlog sourceBlog) {
		return (sourceBlog.getSourceType() == SourceType.RSS || sourceBlog.getSourceType() == SourceType.ATOM)
			&& sourceBlog.getFeedUrl() != null
			&& !sourceBlog.getFeedUrl().isBlank();
	}
}

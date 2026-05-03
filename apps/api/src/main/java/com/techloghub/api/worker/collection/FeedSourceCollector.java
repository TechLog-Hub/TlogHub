package com.techloghub.api.worker.collection;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * source 하나의 외부 feed 호출과 DB 기록을 조율한다.
 */
@Component
@RequiredArgsConstructor
public class FeedSourceCollector {

	private final FeedClient feedClient;
	private final FeedCollectionPersistenceService feedCollectionPersistenceService;
	private final Clock clock;

	/**
	 * source 하나를 수집하고 성공/실패를 collection run으로 남긴다.
	 *
	 * @param sourceBlogId source ID
	 * @return source 수집 결과
	 */
	public FeedCollectionResult collect(Long sourceBlogId) {
		FeedCollectionTarget target;
		try {
			target = feedCollectionPersistenceService.prepare(sourceBlogId, clock.instant());
		} catch (RuntimeException exception) {
			return FeedCollectionResult.failure(sourceBlogId, exception.getMessage());
		}
		try {
			List<FeedEntryCandidate> candidates = feedClient.fetch(target.feedUrl());
			return feedCollectionPersistenceService.recordSuccess(target, candidates, clock.instant());
		} catch (RuntimeException exception) {
			Instant failedAt = clock.instant();
			return feedCollectionPersistenceService.recordFailure(target, exception, failedAt);
		}
	}
}

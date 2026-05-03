package com.techloghub.api.worker.collection;

import java.util.List;

/**
 * 전체 RSS/Atom 수집 실행 결과 요약이다.
 */
public record FeedCollectionSummary(
	int sourceCount,
	int successCount,
	int failureCount,
	int collectedCount,
	int newPostCount,
	int duplicateCount,
	int skippedCount
) {

	public static FeedCollectionSummary from(List<FeedCollectionResult> results) {
		int successCount = (int) results.stream().filter(FeedCollectionResult::success).count();
		int failureCount = results.size() - successCount;
		int collectedCount = results.stream().mapToInt(FeedCollectionResult::collectedCount).sum();
		int newPostCount = results.stream().mapToInt(FeedCollectionResult::newPostCount).sum();
		int duplicateCount = results.stream().mapToInt(FeedCollectionResult::duplicateCount).sum();
		int skippedCount = results.stream().mapToInt(FeedCollectionResult::skippedCount).sum();
		return new FeedCollectionSummary(
			results.size(),
			successCount,
			failureCount,
			collectedCount,
			newPostCount,
			duplicateCount,
			skippedCount
		);
	}
}

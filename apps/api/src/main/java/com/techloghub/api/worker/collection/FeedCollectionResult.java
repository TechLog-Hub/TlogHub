package com.techloghub.api.worker.collection;

/**
 * source 하나의 feed 수집 결과다.
 */
public record FeedCollectionResult(
	Long sourceBlogId,
	boolean success,
	int collectedCount,
	int newPostCount,
	int duplicateCount,
	int skippedCount,
	String failureReason
) {

	public static FeedCollectionResult success(
		Long sourceBlogId,
		int collectedCount,
		int newPostCount,
		int duplicateCount,
		int skippedCount
	) {
		return new FeedCollectionResult(sourceBlogId, true, collectedCount, newPostCount, duplicateCount, skippedCount, null);
	}

	public static FeedCollectionResult failure(Long sourceBlogId, String failureReason) {
		return new FeedCollectionResult(sourceBlogId, false, 0, 0, 0, 0, failureReason);
	}
}

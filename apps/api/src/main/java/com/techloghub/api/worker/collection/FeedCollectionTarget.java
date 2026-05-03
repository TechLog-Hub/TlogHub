package com.techloghub.api.worker.collection;

/**
 * source 수집을 시작한 뒤 외부 feed 호출에 필요한 최소 target 정보다.
 */
public record FeedCollectionTarget(
	Long sourceBlogId,
	Long collectionRunId,
	String feedUrl
) {
}

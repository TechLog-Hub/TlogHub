package com.techloghub.api.worker.collection;

/**
 * 외부 RSS/Atom feed 조회 또는 파싱 실패를 표현한다.
 */
public class FeedFetchException extends RuntimeException {

	public FeedFetchException(String message) {
		super(message);
	}

	public FeedFetchException(String message, Throwable cause) {
		super(message, cause);
	}
}

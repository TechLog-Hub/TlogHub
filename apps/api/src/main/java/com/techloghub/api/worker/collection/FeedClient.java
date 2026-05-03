package com.techloghub.api.worker.collection;

import java.util.List;

/**
 * 외부 feed를 읽어 내부 수집 후보로 변환하는 client 계약이다.
 */
public interface FeedClient {

	/**
	 * 지정한 RSS/Atom feed URL에서 수집 후보를 가져온다.
	 *
	 * @param feedUrl RSS/Atom feed URL
	 * @return 수집 후보 목록
	 */
	List<FeedEntryCandidate> fetch(String feedUrl);
}

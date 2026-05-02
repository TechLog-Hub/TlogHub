package com.techloghub.api.content.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 공개 글 목록처럼 동적 조건과 projection이 필요한 조회 계약이다.
 */
public interface ArchivedPostQueryRepository {

	/**
	 * 공개 글을 검색 조건과 페이지 조건에 맞춰 조회한다.
	 *
	 * @param condition 검색 조건
	 * @param pageable 페이지 요청
	 * @return 공개 글 목록 projection 페이지
	 */
	Page<ArchivedPostListQueryDto> searchPublished(ArchivedPostSearchCondition condition, Pageable pageable);
}

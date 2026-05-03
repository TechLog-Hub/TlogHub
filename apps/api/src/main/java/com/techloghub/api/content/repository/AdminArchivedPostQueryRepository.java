package com.techloghub.api.content.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 관리자 글 목록처럼 운영자 전용 조건과 projection이 필요한 조회 계약이다.
 */
public interface AdminArchivedPostQueryRepository {

	/**
	 * 관리자 글 목록을 검색 조건과 페이지 조건에 맞춰 조회한다.
	 *
	 * @param condition 검색 조건
	 * @param pageable 페이지 요청
	 * @return 관리자 글 목록 projection 페이지
	 */
	Page<AdminArchivedPostQueryDto> searchAdminPosts(AdminArchivedPostSearchCondition condition, Pageable pageable);
}

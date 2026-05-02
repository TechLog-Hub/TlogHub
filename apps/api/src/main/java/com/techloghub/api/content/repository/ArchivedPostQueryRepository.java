package com.techloghub.api.content.repository;

import java.util.List;

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

	/**
	 * root page에 포함된 글 ID 목록의 직군을 조회한다.
	 *
	 * @param postIds 글 ID 목록
	 * @return 글별 직군 projection
	 */
	List<ArchivedPostJobCategoryQueryDto> findJobCategoriesByPostIds(List<Long> postIds);

	/**
	 * root page에 포함된 글 ID 목록의 주제 태그를 조회한다.
	 *
	 * @param postIds 글 ID 목록
	 * @return 글별 주제 태그 projection
	 */
	List<ArchivedPostTopicTagQueryDto> findTopicTagsByPostIds(List<Long> postIds);

	/**
	 * 공개 글 기준 기업별 글 수를 조회한다.
	 *
	 * @return 기업 slug별 글 수
	 */
	List<FilterCountQueryDto> countPublishedPostsByCompany();

	/**
	 * 공개 글 기준 직군별 글 수를 조회한다.
	 *
	 * @return 직군 code별 글 수
	 */
	List<FilterCountQueryDto> countPublishedPostsByJobCategory();

	/**
	 * 공개 글 기준 주제 태그별 글 수를 조회한다.
	 *
	 * @return 태그 slug별 글 수
	 */
	List<FilterCountQueryDto> countPublishedPostsByTopicTag();
}

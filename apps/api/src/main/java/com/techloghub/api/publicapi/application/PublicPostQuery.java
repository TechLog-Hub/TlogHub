package com.techloghub.api.publicapi.application;

import java.util.List;

/**
 * 공개 글 목록 조회 조건이다.
 *
 * @param keyword 제목 검색어
 * @param companySlugs 기업 slug 목록
 * @param jobCodes 직군 코드 목록
 * @param tagSlugs 태그 slug 목록
 * @param sort 정렬 정책
 * @param page 페이지 번호
 * @param size 페이지 크기
 */
public record PublicPostQuery(
	String keyword,
	List<String> companySlugs,
	List<String> jobCodes,
	List<String> tagSlugs,
	PublicPostSort sort,
	int page,
	int size
) {
}

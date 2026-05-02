package com.techloghub.api.content.repository;

/**
 * 공개 글 목록 검색과 필터 조건을 repository 경계에서 표현하는 값 객체다.
 *
 * @param keyword 제목 검색어
 * @param companySlug 기업 slug
 * @param jobCategoryCode 직군 코드
 * @param topicTagSlug 주제 태그 slug
 */
public record ArchivedPostSearchCondition(
	String keyword,
	String companySlug,
	String jobCategoryCode,
	String topicTagSlug
) {
	public static ArchivedPostSearchCondition all() {
		return new ArchivedPostSearchCondition(null, null, null, null);
	}

	public static ArchivedPostSearchCondition of(
		String keyword,
		String companySlug,
		String jobCategoryCode,
		String topicTagSlug
	) {
		return new ArchivedPostSearchCondition(keyword, companySlug, jobCategoryCode, topicTagSlug);
	}
}

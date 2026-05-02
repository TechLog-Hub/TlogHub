package com.techloghub.api.content.repository;

import java.util.List;

/**
 * 공개 글 목록 검색과 필터 조건을 repository 경계에서 표현하는 값 객체다.
 *
 * @param keyword 제목 검색어
 * @param companySlugs 기업 slug 목록
 * @param jobCategoryCodes 직군 코드 목록
 * @param topicTagSlugs 주제 태그 slug 목록
 */
public record ArchivedPostSearchCondition(
	String keyword,
	List<String> companySlugs,
	List<String> jobCategoryCodes,
	List<String> topicTagSlugs
) {
	public static ArchivedPostSearchCondition all() {
		return new ArchivedPostSearchCondition(null, List.of(), List.of(), List.of());
	}

	public static ArchivedPostSearchCondition of(
		String keyword,
		String companySlug,
		String jobCategoryCode,
		String topicTagSlug
	) {
		return new ArchivedPostSearchCondition(
			keyword,
			singletonOrEmpty(companySlug),
			singletonOrEmpty(jobCategoryCode),
			singletonOrEmpty(topicTagSlug)
		);
	}

	public static ArchivedPostSearchCondition of(
		String keyword,
		List<String> companySlugs,
		List<String> jobCategoryCodes,
		List<String> topicTagSlugs
	) {
		return new ArchivedPostSearchCondition(
			keyword,
			List.copyOf(companySlugs),
			List.copyOf(jobCategoryCodes),
			List.copyOf(topicTagSlugs)
		);
	}

	private static List<String> singletonOrEmpty(String value) {
		return value == null || value.isBlank() ? List.of() : List.of(value);
	}
}

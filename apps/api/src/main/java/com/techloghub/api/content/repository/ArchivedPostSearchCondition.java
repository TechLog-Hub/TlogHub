package com.techloghub.api.content.repository;

import java.util.List;

import com.techloghub.api.common.util.CollectionSupport;
import com.techloghub.api.common.util.StringNormalizer;

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
	public ArchivedPostSearchCondition {
		keyword = StringNormalizer.trimToNull(keyword);
		companySlugs = CollectionSupport.nullToEmptyList(companySlugs);
		jobCategoryCodes = CollectionSupport.nullToEmptyList(jobCategoryCodes);
		topicTagSlugs = CollectionSupport.nullToEmptyList(topicTagSlugs);
	}

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
			companySlugs,
			jobCategoryCodes,
			topicTagSlugs
		);
	}

	private static List<String> singletonOrEmpty(String value) {
		String normalizedValue = StringNormalizer.trimToNull(value);
		return normalizedValue == null ? List.of() : List.of(normalizedValue);
	}
}

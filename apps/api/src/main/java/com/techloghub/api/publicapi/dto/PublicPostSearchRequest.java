package com.techloghub.api.publicapi.dto;

import java.util.List;
import java.util.Locale;

import com.techloghub.api.common.error.BusinessException;
import com.techloghub.api.common.error.CommonErrorCode;
import com.techloghub.api.publicapi.application.PublicPostQuery;
import com.techloghub.api.publicapi.application.PublicPostSort;

/**
 * 공개 글 목록 query parameter binding DTO다.
 *
 * @param q 검색어
 * @param company 기업 slug 목록
 * @param job 직군 code 목록
 * @param tag 태그 slug 목록
 * @param sort 정렬 정책
 * @param page 0-based page
 * @param size page size
 */
public record PublicPostSearchRequest(
	String q,
	List<String> company,
	List<String> job,
	List<String> tag,
	String sort,
	Integer page,
	Integer size
) {
	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 24;
	private static final int MAX_SIZE = 100;
	private static final int MAX_FILTER_VALUE_COUNT = 20;
	private static final String DEFAULT_SORT = "latest";

	public PublicPostQuery toQuery() {
		int normalizedPage = page == null ? DEFAULT_PAGE : page;
		int normalizedSize = size == null ? DEFAULT_SIZE : size;
		validatePage(normalizedPage);
		validateSize(normalizedSize);
		List<String> companySlugs = normalizeValues(company, "company");
		List<String> jobCodes = normalizeValues(job, "job");
		List<String> tagSlugs = normalizeValues(tag, "tag");

		return new PublicPostQuery(
			blankToNull(q),
			companySlugs,
			jobCodes,
			tagSlugs,
			parseSort(sort),
			normalizedPage,
			normalizedSize
		);
	}

	private void validatePage(int value) {
		if (value < DEFAULT_PAGE) {
			throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "page must be greater than or equal to 0");
		}
	}

	private void validateSize(int value) {
		if (value < 1 || value > MAX_SIZE) {
			throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "size must be between 1 and " + MAX_SIZE);
		}
	}

	private List<String> normalizeValues(List<String> values, String fieldName) {
		if (values == null || values.isEmpty()) {
			return List.of();
		}
		List<String> normalizedValues = values.stream()
			.map(this::blankToNull)
			.filter(value -> value != null)
			.distinct()
			.toList();
		if (normalizedValues.size() > MAX_FILTER_VALUE_COUNT) {
			throw new BusinessException(
				CommonErrorCode.INVALID_REQUEST,
				fieldName + " filter count must be less than or equal to " + MAX_FILTER_VALUE_COUNT
			);
		}
		return normalizedValues;
	}

	private PublicPostSort parseSort(String value) {
		String normalizedValue = blankToNull(value);
		String sortValue = normalizedValue == null ? DEFAULT_SORT : normalizedValue.toLowerCase(Locale.ROOT);
		return switch (sortValue) {
			case "latest" -> PublicPostSort.LATEST;
			case "relevance" -> PublicPostSort.RELEVANCE;
			default -> throw new BusinessException(
				CommonErrorCode.INVALID_REQUEST,
				"sort must be one of latest, relevance"
			);
		};
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}

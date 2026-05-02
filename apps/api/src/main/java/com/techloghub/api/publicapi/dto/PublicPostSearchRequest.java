package com.techloghub.api.publicapi.dto;

import java.util.List;
import java.util.Locale;

import com.techloghub.api.common.error.BusinessException;
import com.techloghub.api.common.error.CommonErrorCode;
import com.techloghub.api.common.util.CollectionSupport;
import com.techloghub.api.common.util.StringNormalizer;
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
			StringNormalizer.trimToNull(q),
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
			throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "page는 0 이상이어야 합니다.");
		}
	}

	private void validateSize(int value) {
		if (value < 1 || value > MAX_SIZE) {
			throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "size는 1 이상 " + MAX_SIZE + " 이하여야 합니다.");
		}
	}

	private List<String> normalizeValues(List<String> values, String fieldName) {
		if (CollectionSupport.isNullOrEmpty(values)) {
			return List.of();
		}
		List<String> normalizedValues = values.stream()
			.map(StringNormalizer::trimToNull)
			.filter(value -> value != null)
			.distinct()
			.toList();
		if (normalizedValues.size() > MAX_FILTER_VALUE_COUNT) {
			throw new BusinessException(
				CommonErrorCode.INVALID_REQUEST,
				fieldName + " 필터는 최대 " + MAX_FILTER_VALUE_COUNT + "개까지 선택할 수 있습니다."
			);
		}
		return normalizedValues;
	}

	private PublicPostSort parseSort(String value) {
		String normalizedValue = StringNormalizer.trimToNull(value);
		String sortValue = normalizedValue == null ? DEFAULT_SORT : normalizedValue.toLowerCase(Locale.ROOT);
		return switch (sortValue) {
			case "latest" -> PublicPostSort.LATEST;
			case "relevance" -> PublicPostSort.RELEVANCE;
			default -> throw new BusinessException(
				CommonErrorCode.INVALID_REQUEST,
				"sort는 latest 또는 relevance만 사용할 수 있습니다."
			);
		};
	}
}

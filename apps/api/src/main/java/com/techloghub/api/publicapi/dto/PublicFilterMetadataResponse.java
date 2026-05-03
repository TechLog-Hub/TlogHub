package com.techloghub.api.publicapi.dto;

import java.util.List;

import com.techloghub.api.common.util.CollectionSupport;

/**
 * 공개 목록 화면의 필터 메타데이터 응답이다.
 *
 * @param companies 기업 필터 옵션
 * @param jobs 직군 필터 옵션
 * @param tags 주제 태그 필터 옵션
 */
public record PublicFilterMetadataResponse(
	List<PublicCompanyFilterResponse> companies,
	List<PublicJobCategoryFilterResponse> jobs,
	List<PublicTopicTagFilterResponse> tags
) {
	public PublicFilterMetadataResponse {
		companies = CollectionSupport.nullToEmptyList(companies);
		jobs = CollectionSupport.nullToEmptyList(jobs);
		tags = CollectionSupport.nullToEmptyList(tags);
	}

	public static PublicFilterMetadataResponse of(
		List<PublicCompanyFilterResponse> companies,
		List<PublicJobCategoryFilterResponse> jobs,
		List<PublicTopicTagFilterResponse> tags
	) {
		return new PublicFilterMetadataResponse(companies, jobs, tags);
	}
}

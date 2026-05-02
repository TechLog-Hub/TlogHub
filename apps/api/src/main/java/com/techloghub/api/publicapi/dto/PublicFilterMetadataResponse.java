package com.techloghub.api.publicapi.dto;

import java.util.List;

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
		companies = List.copyOf(companies == null ? List.of() : companies);
		jobs = List.copyOf(jobs == null ? List.of() : jobs);
		tags = List.copyOf(tags == null ? List.of() : tags);
	}

	public static PublicFilterMetadataResponse of(
		List<PublicCompanyFilterResponse> companies,
		List<PublicJobCategoryFilterResponse> jobs,
		List<PublicTopicTagFilterResponse> tags
	) {
		return new PublicFilterMetadataResponse(companies, jobs, tags);
	}
}

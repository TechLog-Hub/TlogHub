package com.techloghub.api.publicapi.dto;

import com.techloghub.api.content.domain.Company;

/**
 * 공개 API에서 사용하는 기업 요약 응답이다.
 *
 * @param slug 기업 slug
 * @param name 기업 이름
 */
public record PublicCompanySummaryResponse(
	String slug,
	String name
) {
	public static PublicCompanySummaryResponse from(Company company) {
		return of(company.getSlug(), company.getNameKo());
	}

	public static PublicCompanySummaryResponse of(String slug, String name) {
		return new PublicCompanySummaryResponse(slug, name);
	}
}

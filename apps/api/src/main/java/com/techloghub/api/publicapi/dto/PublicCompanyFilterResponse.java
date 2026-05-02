package com.techloghub.api.publicapi.dto;

import com.techloghub.api.content.domain.Company;

/**
 * 공개 필터의 기업 옵션이다.
 *
 * @param slug 기업 slug
 * @param name 기업 이름
 * @param count 공개 글 수
 */
public record PublicCompanyFilterResponse(
	String slug,
	String name,
	long count
) {
	public static PublicCompanyFilterResponse of(Company company, long count) {
		return new PublicCompanyFilterResponse(company.getSlug(), company.getNameKo(), count);
	}
}

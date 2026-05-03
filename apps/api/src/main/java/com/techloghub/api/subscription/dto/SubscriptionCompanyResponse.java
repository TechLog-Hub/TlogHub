package com.techloghub.api.subscription.dto;

import com.techloghub.api.content.domain.Company;

/**
 * 구독 관리 응답의 기업 요약이다.
 *
 * @param slug 기업 slug
 * @param name 기업 이름
 */
public record SubscriptionCompanyResponse(
	String slug,
	String name
) {
	public static SubscriptionCompanyResponse from(Company company) {
		return new SubscriptionCompanyResponse(company.getSlug(), company.getNameKo());
	}
}

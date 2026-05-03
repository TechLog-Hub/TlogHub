package com.techloghub.api.subscription.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * 구독 관리 화면에서 기업 구독을 추가하는 요청이다.
 *
 * @param companySlugs 추가할 기업 slug 목록
 */
public record SubscriptionCompanyAddRequest(
	@NotEmpty(message = "companySlugs는 1개 이상 입력해야 합니다.")
	@Size(max = 20, message = "companySlugs는 최대 20개까지 입력할 수 있습니다.")
	List<@NotBlank(message = "companySlug는 비어 있을 수 없습니다.") String> companySlugs
) {
}

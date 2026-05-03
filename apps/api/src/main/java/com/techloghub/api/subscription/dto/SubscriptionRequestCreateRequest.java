package com.techloghub.api.subscription.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * 이메일 기반 기업 구독 요청이다.
 *
 * @param email 구독자 이메일
 * @param companySlugs 구독할 기업 slug 목록
 */
public record SubscriptionRequestCreateRequest(
	@NotBlank(message = "email은 필수입니다.")
	@Email(message = "email 형식이 올바르지 않습니다.")
	@Size(max = 320, message = "email은 최대 320자까지 입력할 수 있습니다.")
	String email,

	@NotEmpty(message = "companySlugs는 1개 이상 입력해야 합니다.")
	@Size(max = 20, message = "companySlugs는 최대 20개까지 입력할 수 있습니다.")
	List<@NotBlank(message = "companySlug는 비어 있을 수 없습니다.") String> companySlugs
) {
}

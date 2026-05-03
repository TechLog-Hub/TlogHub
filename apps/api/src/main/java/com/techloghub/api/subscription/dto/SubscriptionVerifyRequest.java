package com.techloghub.api.subscription.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 이메일 구독 확인 요청이다.
 *
 * @param token 확인 토큰 원문
 */
public record SubscriptionVerifyRequest(
	@NotBlank(message = "token은 필수입니다.")
	String token
) {
}

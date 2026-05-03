package com.techloghub.api.subscription.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 구독 요청 생성 결과다.
 *
 * @param requestAccepted 요청 접수 여부
 * @param maskedEmail 마스킹된 이메일
 * @param verificationToken local/test 환경에서만 노출하는 확인 토큰
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscriptionRequestResponse(
	boolean requestAccepted,
	String maskedEmail,
	String verificationToken
) {
	public static SubscriptionRequestResponse accepted(String maskedEmail, String verificationToken) {
		return new SubscriptionRequestResponse(true, maskedEmail, verificationToken);
	}
}

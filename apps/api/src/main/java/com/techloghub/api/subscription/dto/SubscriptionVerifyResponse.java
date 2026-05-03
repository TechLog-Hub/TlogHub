package com.techloghub.api.subscription.dto;

import java.util.Locale;

import com.techloghub.api.subscription.domain.Subscriber;

/**
 * 구독 확인 성공 응답이다.
 *
 * @param status 구독자 상태
 * @param manageToken 구독 관리 토큰 원문
 */
public record SubscriptionVerifyResponse(
	String status,
	String manageToken
) {
	public static SubscriptionVerifyResponse of(Subscriber subscriber, String manageToken) {
		return new SubscriptionVerifyResponse(
			subscriber.getStatus().name().toLowerCase(Locale.ROOT),
			manageToken
		);
	}
}

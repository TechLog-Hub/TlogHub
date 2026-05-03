package com.techloghub.api.subscription.dto;

import java.util.List;
import java.util.Locale;

import com.techloghub.api.common.util.CollectionSupport;
import com.techloghub.api.common.util.MaskingSupport;
import com.techloghub.api.subscription.domain.CompanySubscription;
import com.techloghub.api.subscription.domain.Subscriber;

/**
 * 구독 관리 화면에서 사용하는 현재 구독 상태 응답이다.
 *
 * @param maskedEmail 마스킹된 이메일
 * @param status 구독자 상태
 * @param companies 활성 구독 기업 목록
 */
public record SubscriptionManageResponse(
	String maskedEmail,
	String status,
	List<SubscriptionCompanyResponse> companies
) {
	public SubscriptionManageResponse {
		companies = CollectionSupport.nullToEmptyList(companies);
	}

	public static SubscriptionManageResponse of(
		Subscriber subscriber,
		List<CompanySubscription> activeSubscriptions
	) {
		List<SubscriptionCompanyResponse> companies = activeSubscriptions.stream()
			.map(CompanySubscription::getCompany)
			.map(SubscriptionCompanyResponse::from)
			.toList();
		return new SubscriptionManageResponse(
			MaskingSupport.maskEmail(subscriber.getEmail()),
			subscriber.getStatus().name().toLowerCase(Locale.ROOT),
			companies
		);
	}
}

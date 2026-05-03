package com.techloghub.api.subscription.error;

import org.springframework.http.HttpStatus;

import com.techloghub.api.common.error.ErrorCode;
import com.techloghub.api.common.error.ErrorLogLevel;

import lombok.RequiredArgsConstructor;

/**
 * 구독 API에서 사용하는 domain-specific error code다.
 */
@RequiredArgsConstructor
public enum SubscriptionErrorCode implements ErrorCode {
	SUBSCRIPTION_COMPANY_NOT_FOUND(
		HttpStatus.BAD_REQUEST,
		"SUBSCRIPTION_COMPANY_NOT_FOUND",
		"구독할 수 없는 기업이 포함되어 있습니다."
	),
	SUBSCRIPTION_VERIFICATION_TOKEN_INVALID(
		HttpStatus.BAD_REQUEST,
		"SUBSCRIPTION_VERIFICATION_TOKEN_INVALID",
		"구독 확인 링크가 올바르지 않습니다."
	),
	SUBSCRIPTION_VERIFICATION_TOKEN_EXPIRED(
		HttpStatus.BAD_REQUEST,
		"SUBSCRIPTION_VERIFICATION_TOKEN_EXPIRED",
		"구독 확인 링크가 만료되었습니다."
	),
	SUBSCRIPTION_MANAGE_TOKEN_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"SUBSCRIPTION_MANAGE_TOKEN_NOT_FOUND",
		"구독 관리 정보를 찾을 수 없습니다."
	),
	SUBSCRIPTION_NOT_ACTIVE(
		HttpStatus.CONFLICT,
		"SUBSCRIPTION_NOT_ACTIVE",
		"활성 구독 상태에서만 변경할 수 있습니다."
	);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	@Override
	public HttpStatus httpStatus() {
		return httpStatus;
	}

	@Override
	public String code() {
		return code;
	}

	@Override
	public String message() {
		return message;
	}

	@Override
	public ErrorLogLevel logLevel() {
		return ErrorLogLevel.INFO;
	}
}

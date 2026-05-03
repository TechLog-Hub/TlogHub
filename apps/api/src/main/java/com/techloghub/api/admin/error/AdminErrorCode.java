package com.techloghub.api.admin.error;

import org.springframework.http.HttpStatus;

import com.techloghub.api.common.error.ErrorCode;
import com.techloghub.api.common.error.ErrorLogLevel;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 API의 domain-specific error code다.
 */
@RequiredArgsConstructor
public enum AdminErrorCode implements ErrorCode {
	ADMIN_INVALID_CREDENTIALS(
		HttpStatus.UNAUTHORIZED,
		"ADMIN_INVALID_CREDENTIALS",
		"관리자 이메일 또는 비밀번호가 올바르지 않습니다."
	),
	ADMIN_SESSION_INVALID(
		HttpStatus.UNAUTHORIZED,
		"ADMIN_SESSION_INVALID",
		"관리자 인증이 필요합니다."
	),
	ADMIN_POST_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"ADMIN_POST_NOT_FOUND",
		"관리자 글 정보를 찾을 수 없습니다."
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

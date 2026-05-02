package com.techloghub.api.common.error;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

/**
 * 모든 API에서 공통으로 사용하는 오류 코드다.
 */
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 값이 올바르지 않습니다."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "예상하지 못한 오류가 발생했습니다.");

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
}

package com.techloghub.api.common.error;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

/**
 * 모든 API에서 공통으로 사용하는 오류 코드다.
 */
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 값이 올바르지 않습니다.", ErrorLogLevel.INFO),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다.", ErrorLogLevel.INFO),
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다.", ErrorLogLevel.INFO),
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다.", ErrorLogLevel.INFO),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다.", ErrorLogLevel.INFO),
	CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "요청 상태가 현재 리소스 상태와 충돌합니다.", ErrorLogLevel.INFO),
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "지원하지 않는 Content-Type입니다.", ErrorLogLevel.INFO),
	TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.", ErrorLogLevel.WARN),
	EXTERNAL_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_SERVICE_UNAVAILABLE", "외부 서비스가 일시적으로 불안정합니다.", ErrorLogLevel.WARN),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "예상하지 못한 오류가 발생했습니다.", ErrorLogLevel.ERROR);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
	private final ErrorLogLevel logLevel;

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
		return logLevel;
	}
}

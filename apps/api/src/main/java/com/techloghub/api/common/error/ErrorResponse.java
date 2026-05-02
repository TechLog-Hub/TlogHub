package com.techloghub.api.common.error;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * API 공통 오류 응답이다.
 *
 * @param code machine-readable 오류 코드
 * @param message 기본 오류 메시지
 * @param errors 필드 단위 오류 목록
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
	String code,
	String message,
	List<FieldErrorResponse> errors
) {
	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.code(), errorCode.message(), List.of());
	}

	public static ErrorResponse of(ErrorCode errorCode, String message) {
		return new ErrorResponse(errorCode.code(), message, List.of());
	}

	public static ErrorResponse of(ErrorCode errorCode, List<FieldErrorResponse> errors) {
		return new ErrorResponse(errorCode.code(), errorCode.message(), List.copyOf(errors));
	}
}

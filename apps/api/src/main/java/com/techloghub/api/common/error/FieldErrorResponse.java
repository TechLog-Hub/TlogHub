package com.techloghub.api.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 요청 필드 단위 validation/binding 실패 정보다.
 *
 * @param field 필드 경로
 * @param reason 실패 사유
 * @param rejectedValue 안전하게 노출 가능한 거절 값
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FieldErrorResponse(
	String field,
	String reason,
	String rejectedValue
) {
	public FieldErrorResponse(String field, String reason) {
		this(field, reason, null);
	}

	public static FieldErrorResponse of(String field, String reason) {
		return new FieldErrorResponse(field, reason, null);
	}

	public static FieldErrorResponse of(String field, String reason, String rejectedValue) {
		return new FieldErrorResponse(field, reason, rejectedValue);
	}
}

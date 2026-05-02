package com.techloghub.api.common.error;

/**
 * 요청 필드 단위 validation/binding 실패 정보다.
 *
 * @param field 필드 경로
 * @param reason 실패 사유
 */
public record FieldErrorResponse(
	String field,
	String reason
) {
}

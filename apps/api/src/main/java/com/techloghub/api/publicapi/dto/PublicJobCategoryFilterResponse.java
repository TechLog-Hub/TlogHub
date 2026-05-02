package com.techloghub.api.publicapi.dto;

/**
 * 공개 필터의 직군 옵션이다.
 *
 * @param code 직군 코드
 * @param label 직군 라벨
 * @param count 공개 글 수
 */
public record PublicJobCategoryFilterResponse(
	String code,
	String label,
	long count
) {
}

package com.techloghub.api.publicapi.dto;

/**
 * 공개 API에서 사용하는 기업 요약 응답이다.
 *
 * @param slug 기업 slug
 * @param name 기업 이름
 */
public record PublicCompanySummaryResponse(
	String slug,
	String name
) {
}

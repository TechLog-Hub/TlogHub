package com.techloghub.api.publicapi.dto;

/**
 * 공개 API에서 사용하는 소스 블로그 요약 응답이다.
 *
 * @param name 소스 이름
 * @param url 소스 홈페이지 URL
 */
public record PublicSourceSummaryResponse(
	String name,
	String url
) {
}

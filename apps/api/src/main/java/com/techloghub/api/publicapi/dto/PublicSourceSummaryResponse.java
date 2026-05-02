package com.techloghub.api.publicapi.dto;

import com.techloghub.api.content.domain.SourceBlog;

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
	public static PublicSourceSummaryResponse from(SourceBlog sourceBlog) {
		return of(sourceBlog.getName(), sourceBlog.getHomepageUrl());
	}

	public static PublicSourceSummaryResponse of(String name, String url) {
		return new PublicSourceSummaryResponse(name, url);
	}
}

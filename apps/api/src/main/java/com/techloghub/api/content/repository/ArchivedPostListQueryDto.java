package com.techloghub.api.content.repository;

import java.time.Instant;

/**
 * 공개 글 목록 화면에서 필요한 최소 필드를 담는 조회 전용 projection이다.
 *
 * @param id 글 ID
 * @param slug 글 slug
 * @param title 글 제목
 * @param originUrl 원문 URL
 * @param companySlug 기업 slug
 * @param companyNameKo 기업 한국어 이름
 * @param sourceBlogName 소스 블로그 이름
 * @param publishedAt 원문 게시 시각
 */
public record ArchivedPostListQueryDto(
	Long id,
	String slug,
	String title,
	String originUrl,
	String companySlug,
	String companyNameKo,
	String sourceBlogName,
	Instant publishedAt
) {
}

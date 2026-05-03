package com.techloghub.api.content.repository;

import java.time.Instant;

import com.techloghub.api.content.domain.SourceBlogStatus;
import com.techloghub.api.content.domain.SourceType;

/**
 * 관리자 소스 목록 화면에서 사용하는 조회 projection이다.
 */
public record AdminSourceBlogQueryDto(
	Long id,
	String companySlug,
	String companyNameKo,
	String name,
	String homepageUrl,
	String feedUrl,
	SourceType sourceType,
	SourceBlogStatus status,
	String reviewReason,
	Instant lastCollectedAt,
	Instant createdAt
) {
}

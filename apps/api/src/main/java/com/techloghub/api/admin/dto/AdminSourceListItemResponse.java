package com.techloghub.api.admin.dto;

import java.time.Instant;
import java.util.Locale;

import com.techloghub.api.content.repository.AdminSourceBlogQueryDto;

/**
 * 관리자 소스 목록 항목 응답이다.
 */
public record AdminSourceListItemResponse(
	Long id,
	String companySlug,
	String companyName,
	String name,
	String homepageUrl,
	String feedUrl,
	String sourceType,
	String status,
	String reviewReason,
	Instant lastCollectedAt,
	Instant createdAt
) {
	public static AdminSourceListItemResponse from(AdminSourceBlogQueryDto source) {
		return new AdminSourceListItemResponse(
			source.id(),
			source.companySlug(),
			source.companyNameKo(),
			source.name(),
			source.homepageUrl(),
			source.feedUrl(),
			source.sourceType().name().toLowerCase(Locale.ROOT),
			source.status().name().toLowerCase(Locale.ROOT),
			source.reviewReason(),
			source.lastCollectedAt(),
			source.createdAt()
		);
	}
}

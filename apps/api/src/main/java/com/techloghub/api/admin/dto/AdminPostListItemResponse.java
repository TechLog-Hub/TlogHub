package com.techloghub.api.admin.dto;

import java.time.Instant;
import java.util.Locale;

import com.techloghub.api.content.domain.SummaryState;
import com.techloghub.api.content.repository.AdminArchivedPostQueryDto;

/**
 * 관리자 글 목록 항목 응답이다.
 */
public record AdminPostListItemResponse(
	Long id,
	String slug,
	String title,
	String originUrl,
	String companySlug,
	String companyName,
	String sourceName,
	Instant publishedAt,
	Instant collectedAt,
	String processingState,
	String visibilityState,
	String summaryState,
	String reviewReason
) {
	public static AdminPostListItemResponse from(AdminArchivedPostQueryDto post) {
		return new AdminPostListItemResponse(
			post.id(),
			post.slug(),
			post.title(),
			post.originUrl(),
			post.companySlug(),
			post.companyNameKo(),
			post.sourceBlogName(),
			post.publishedAt(),
			post.collectedAt(),
			post.processingState().name().toLowerCase(Locale.ROOT),
			post.visibilityState().name().toLowerCase(Locale.ROOT),
			summaryState(post.summaryState()),
			post.reviewReason()
		);
	}

	private static String summaryState(SummaryState value) {
		return value == null ? "none" : value.name().toLowerCase(Locale.ROOT);
	}
}

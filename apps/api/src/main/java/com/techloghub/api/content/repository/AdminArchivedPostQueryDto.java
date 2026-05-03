package com.techloghub.api.content.repository;

import java.time.Instant;

import com.techloghub.api.content.domain.ProcessingState;
import com.techloghub.api.content.domain.SummaryState;
import com.techloghub.api.content.domain.VisibilityState;

/**
 * 관리자 글 목록 화면에서 사용하는 조회 projection이다.
 */
public record AdminArchivedPostQueryDto(
	Long id,
	String slug,
	String title,
	String originUrl,
	String companySlug,
	String companyNameKo,
	String sourceBlogName,
	Instant publishedAt,
	Instant collectedAt,
	ProcessingState processingState,
	VisibilityState visibilityState,
	String reviewReason,
	SummaryState summaryState
) {
}

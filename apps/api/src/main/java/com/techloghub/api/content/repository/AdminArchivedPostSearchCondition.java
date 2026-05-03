package com.techloghub.api.content.repository;

import com.techloghub.api.common.util.StringNormalizer;
import com.techloghub.api.content.domain.ProcessingState;
import com.techloghub.api.content.domain.SummaryState;
import com.techloghub.api.content.domain.VisibilityState;

/**
 * 관리자 글 목록 검색 조건이다.
 */
public record AdminArchivedPostSearchCondition(
	String keyword,
	String companySlug,
	ProcessingState processingState,
	VisibilityState visibilityState,
	SummaryState summaryState
) {
	public AdminArchivedPostSearchCondition {
		keyword = StringNormalizer.trimToNull(keyword);
		companySlug = StringNormalizer.trimToNull(companySlug);
	}

	public static AdminArchivedPostSearchCondition of(
		String keyword,
		String companySlug,
		ProcessingState processingState,
		VisibilityState visibilityState,
		SummaryState summaryState
	) {
		return new AdminArchivedPostSearchCondition(
			keyword,
			companySlug,
			processingState,
			visibilityState,
			summaryState
		);
	}
}

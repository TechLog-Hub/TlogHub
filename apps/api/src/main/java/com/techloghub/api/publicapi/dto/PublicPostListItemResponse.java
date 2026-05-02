package com.techloghub.api.publicapi.dto;

import java.time.Instant;
import java.util.List;

import com.techloghub.api.content.repository.ArchivedPostListQueryDto;

/**
 * 공개 글 목록 항목 응답이다.
 *
 * @param id 글 ID
 * @param slug 글 slug
 * @param title 글 제목
 * @param company 기업 요약
 * @param publishedAt 원문 게시 시각
 * @param jobCategories 직군 코드 목록
 * @param topicTags 주제 태그 slug 목록
 * @param summaryState 요약 상태
 * @param summaryPreview 요약 미리보기
 * @param originUrl 원문 URL
 */
public record PublicPostListItemResponse(
	Long id,
	String slug,
	String title,
	PublicCompanySummaryResponse company,
	Instant publishedAt,
	List<String> jobCategories,
	List<String> topicTags,
	String summaryState,
	String summaryPreview,
	String originUrl
) {
	public PublicPostListItemResponse {
		jobCategories = List.copyOf(jobCategories == null ? List.of() : jobCategories);
		topicTags = List.copyOf(topicTags == null ? List.of() : topicTags);
	}

	public static PublicPostListItemResponse of(
		ArchivedPostListQueryDto post,
		List<String> jobCategories,
		List<String> topicTags,
		String summaryState,
		String summaryPreview
	) {
		return new PublicPostListItemResponse(
			post.id(),
			post.slug(),
			post.title(),
			PublicCompanySummaryResponse.of(post.companySlug(), post.companyNameKo()),
			post.publishedAt(),
			jobCategories,
			topicTags,
			summaryState,
			summaryPreview,
			post.originUrl()
		);
	}
}

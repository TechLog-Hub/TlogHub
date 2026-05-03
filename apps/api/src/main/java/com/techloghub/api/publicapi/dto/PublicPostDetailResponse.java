package com.techloghub.api.publicapi.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techloghub.api.common.util.CollectionSupport;
import com.techloghub.api.content.domain.ArchivedPost;

/**
 * 공개 글 상세 응답이다.
 *
 * @param id 글 ID
 * @param slug 글 slug
 * @param title 글 제목
 * @param company 기업 요약
 * @param source 소스 블로그 요약
 * @param publishedAt 원문 게시 시각
 * @param jobCategories 직군 코드 목록
 * @param topicTags 주제 태그 slug 목록
 * @param summaryState 요약 상태
 * @param summary AI 요약
 * @param originUrl 원문 URL
 * @param aiNotice AI 요약 안내 문구
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PublicPostDetailResponse(
	Long id,
	String slug,
	String title,
	PublicCompanySummaryResponse company,
	PublicSourceSummaryResponse source,
	Instant publishedAt,
	List<String> jobCategories,
	List<String> topicTags,
	String summaryState,
	PublicPostSummaryResponse summary,
	String originUrl,
	String aiNotice
) {
	public PublicPostDetailResponse {
		jobCategories = CollectionSupport.nullToEmptyList(jobCategories);
		topicTags = CollectionSupport.nullToEmptyList(topicTags);
	}

	public static PublicPostDetailResponse of(
		ArchivedPost post,
		List<String> jobCategories,
		List<String> topicTags,
		String summaryState,
		PublicPostSummaryResponse summary,
		String aiNotice
	) {
		return new PublicPostDetailResponse(
			post.getId(),
			post.getSlug(),
			post.getTitle(),
			PublicCompanySummaryResponse.from(post.getCompany()),
			PublicSourceSummaryResponse.from(post.getSourceBlog()),
			post.getPublishedAt(),
			jobCategories,
			topicTags,
			summaryState,
			summary,
			post.getOriginUrl(),
			aiNotice
		);
	}
}

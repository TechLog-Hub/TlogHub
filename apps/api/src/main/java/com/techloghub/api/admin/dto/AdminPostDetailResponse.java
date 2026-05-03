package com.techloghub.api.admin.dto;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import com.techloghub.api.content.domain.ArchivedPost;
import com.techloghub.api.content.domain.JobCategory;
import com.techloghub.api.content.domain.TopicTag;

/**
 * 관리자 글 상세 응답이다.
 */
public record AdminPostDetailResponse(
	Long id,
	String slug,
	String title,
	String originUrl,
	String canonicalUrl,
	String companySlug,
	String companyName,
	String sourceName,
	Instant publishedAt,
	Instant collectedAt,
	String processingState,
	String visibilityState,
	String reviewReason,
	List<String> jobCategories,
	List<String> topicTags
) {
	public static AdminPostDetailResponse from(ArchivedPost post) {
		return new AdminPostDetailResponse(
			post.getId(),
			post.getSlug(),
			post.getTitle(),
			post.getOriginUrl(),
			post.getCanonicalUrl(),
			post.getCompany().getSlug(),
			post.getCompany().getNameKo(),
			post.getSourceBlog().getName(),
			post.getPublishedAt(),
			post.getCollectedAt(),
			post.getProcessingState().name().toLowerCase(Locale.ROOT),
			post.getVisibilityState().name().toLowerCase(Locale.ROOT),
			post.getReviewReason(),
			post.getJobCategories().stream()
				.map(JobCategory::getCode)
				.sorted()
				.toList(),
			post.getTopicTags().stream()
				.map(TopicTag::getSlug)
				.sorted()
				.toList()
		);
	}
}

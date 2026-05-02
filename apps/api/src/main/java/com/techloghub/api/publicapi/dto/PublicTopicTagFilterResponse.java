package com.techloghub.api.publicapi.dto;

import com.techloghub.api.content.domain.TopicTag;

/**
 * 공개 필터의 주제 태그 옵션이다.
 *
 * @param slug 태그 slug
 * @param label 태그 라벨
 * @param count 공개 글 수
 */
public record PublicTopicTagFilterResponse(
	String slug,
	String label,
	long count
) {
	public static PublicTopicTagFilterResponse of(TopicTag tag, long count) {
		return new PublicTopicTagFilterResponse(tag.getSlug(), tag.getLabel(), count);
	}
}

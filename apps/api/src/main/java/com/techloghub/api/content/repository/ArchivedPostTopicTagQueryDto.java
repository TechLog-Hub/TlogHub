package com.techloghub.api.content.repository;

/**
 * 공개 글 목록의 주제 태그 보조 데이터를 root page 이후 in-memory join 하기 위한 projection이다.
 *
 * @param postId 글 ID
 * @param slug 태그 slug
 * @param label 태그 라벨
 */
public record ArchivedPostTopicTagQueryDto(
	Long postId,
	String slug,
	String label
) {
}

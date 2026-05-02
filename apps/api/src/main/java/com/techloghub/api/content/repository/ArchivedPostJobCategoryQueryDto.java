package com.techloghub.api.content.repository;

/**
 * 공개 글 목록의 직군 보조 데이터를 root page 이후 in-memory join 하기 위한 projection이다.
 *
 * @param postId 글 ID
 * @param code 직군 코드
 * @param labelKo 직군 한국어 라벨
 */
public record ArchivedPostJobCategoryQueryDto(
	Long postId,
	String code,
	String labelKo
) {
}

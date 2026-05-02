package com.techloghub.api.content.repository;

/**
 * 공개 필터 옵션의 published 글 수를 나타내는 projection이다.
 *
 * @param key company slug, job code, tag slug
 * @param count published 글 수
 */
public record FilterCountQueryDto(
	String key,
	long count
) {
}

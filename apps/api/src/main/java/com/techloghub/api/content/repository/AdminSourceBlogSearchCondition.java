package com.techloghub.api.content.repository;

import com.techloghub.api.common.util.StringNormalizer;
import com.techloghub.api.content.domain.SourceBlogStatus;

/**
 * 관리자 소스 목록 검색 조건이다.
 */
public record AdminSourceBlogSearchCondition(
	String keyword,
	SourceBlogStatus status
) {
	public AdminSourceBlogSearchCondition {
		keyword = StringNormalizer.trimToNull(keyword);
	}

	public static AdminSourceBlogSearchCondition of(String keyword, SourceBlogStatus status) {
		return new AdminSourceBlogSearchCondition(keyword, status);
	}
}

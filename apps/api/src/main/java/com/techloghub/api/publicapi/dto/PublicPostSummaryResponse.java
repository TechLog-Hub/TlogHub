package com.techloghub.api.publicapi.dto;

import java.util.List;

/**
 * 공개 글 상세의 AI 요약 응답이다.
 *
 * @param headline 한 줄 요약
 * @param bullets 핵심 bullet 목록
 */
public record PublicPostSummaryResponse(
	String headline,
	List<String> bullets
) {
}

package com.techloghub.api.admin.dto;

import java.time.Instant;

import com.techloghub.api.content.domain.CollectionRun;

/**
 * 관리자 화면에서 보는 최근 수집 실패 응답이다.
 */
public record AdminFailureResponse(
	Long collectionRunId,
	Long sourceBlogId,
	String sourceName,
	String status,
	Instant startedAt,
	Instant finishedAt,
	String failureReason
) {

	public static AdminFailureResponse from(CollectionRun collectionRun) {
		return new AdminFailureResponse(
			collectionRun.getId(),
			collectionRun.getSourceBlog().getId(),
			collectionRun.getSourceBlog().getName(),
			collectionRun.getStatus().name().toLowerCase(),
			collectionRun.getStartedAt(),
			collectionRun.getFinishedAt(),
			collectionRun.getFailureReason()
		);
	}
}

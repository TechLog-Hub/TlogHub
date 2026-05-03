package com.techloghub.api.admin.dto;

import java.time.Instant;

import org.springframework.batch.core.JobExecution;

/**
 * 관리자 Batch job 실행 응답이다.
 */
public record AdminJobRunResponse(
	String jobName,
	Long executionId,
	String status,
	Instant requestedAt
) {

	public static AdminJobRunResponse from(JobExecution jobExecution) {
		return new AdminJobRunResponse(
			jobExecution.getJobInstance().getJobName(),
			jobExecution.getId(),
			jobExecution.getStatus().name().toLowerCase(),
			Instant.parse(jobExecution.getJobParameters().getString("requestedAt"))
		);
	}
}

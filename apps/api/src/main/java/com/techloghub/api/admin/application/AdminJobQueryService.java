package com.techloghub.api.admin.application;

import java.util.List;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techloghub.api.admin.dto.AdminFailureResponse;
import com.techloghub.api.admin.dto.AdminJobRunResponse;
import com.techloghub.api.admin.error.AdminErrorCode;
import com.techloghub.api.common.error.BusinessException;
import com.techloghub.api.content.domain.CollectionRunStatus;
import com.techloghub.api.content.repository.CollectionRunRepository;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 Batch job 조회 use-case다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminJobQueryService {

	private final JobExplorer jobExplorer;
	private final CollectionRunRepository collectionRunRepository;

	public AdminJobRunResponse getJobExecution(Long executionId) {
		JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
		if (jobExecution == null) {
			throw new BusinessException(AdminErrorCode.ADMIN_BATCH_JOB_NOT_FOUND);
		}
		return AdminJobRunResponse.from(jobExecution);
	}

	public List<AdminFailureResponse> getRecentCollectionFailures(int size) {
		return collectionRunRepository.findByStatusOrderByStartedAtDesc(
				CollectionRunStatus.FAILED,
				PageRequest.of(0, size)
			)
			.stream()
			.map(AdminFailureResponse::from)
			.toList();
	}
}

package com.techloghub.api.admin.api;

import static com.techloghub.api.admin.application.AdminAuthenticationService.ADMIN_USER_REQUEST_ATTRIBUTE;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techloghub.api.admin.application.AdminJobQueryService;
import com.techloghub.api.admin.domain.AdminUser;
import com.techloghub.api.admin.dto.AdminFailureResponse;
import com.techloghub.api.admin.dto.AdminJobRunResponse;
import com.techloghub.api.admin.error.AdminErrorCode;
import com.techloghub.api.common.error.BusinessException;
import com.techloghub.api.worker.batch.BatchJobAlreadyRunningException;
import com.techloghub.api.worker.batch.BatchJobLaunchException;
import com.techloghub.api.worker.batch.BatchJobLauncherService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 Batch job 운영 API다.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/jobs")
public class AdminJobController {

	private static final String DEFAULT_REASON = "manual-admin-run";

	private final BatchJobLauncherService batchJobLauncherService;
	private final AdminJobQueryService adminJobQueryService;

	@PostMapping("/collect/run")
	public AdminJobRunResponse runCollectionJob(
		@RequestAttribute(ADMIN_USER_REQUEST_ATTRIBUTE) AdminUser adminUser,
		@RequestParam(required = false) @Positive Long sourceId,
		@RequestParam(required = false, defaultValue = DEFAULT_REASON) String reason
	) {
		try {
			return AdminJobRunResponse.from(batchJobLauncherService.runRssFeedCollection(
				sourceId,
				"admin:" + adminUser.getId(),
				reason
			));
		} catch (BatchJobAlreadyRunningException exception) {
			throw new BusinessException(AdminErrorCode.ADMIN_BATCH_JOB_ALREADY_RUNNING, exception);
		} catch (BatchJobLaunchException exception) {
			throw new BusinessException(AdminErrorCode.ADMIN_BATCH_JOB_LAUNCH_FAILED, exception);
		}
	}

	@GetMapping("/executions/{executionId}")
	public AdminJobRunResponse getJobExecution(
		@PathVariable @Positive Long executionId
	) {
		return adminJobQueryService.getJobExecution(executionId);
	}

	@GetMapping("/failures")
	public List<AdminFailureResponse> getCollectionFailures(
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
	) {
		return adminJobQueryService.getRecentCollectionFailures(size);
	}
}

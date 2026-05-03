package com.techloghub.api.worker.batch;

import java.time.Clock;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.techloghub.api.common.util.StringNormalizer;

/**
 * 관리자 API와 scheduler가 공유하는 Batch job 실행 진입점이다.
 */
@Service
public class BatchJobLauncherService {

	private static final int PARAMETER_MAX_LENGTH = 200;

	private final JobLauncher jobLauncher;
	private final BatchRunGuard batchRunGuard;
	private final Clock clock;
	private final Job rssFeedCollectionJob;

	public BatchJobLauncherService(
		@Qualifier("asyncBatchJobLauncher") JobLauncher jobLauncher,
		BatchRunGuard batchRunGuard,
		Clock clock,
		@Qualifier(BatchJobNames.RSS_FEED_COLLECTION_JOB) Job rssFeedCollectionJob
	) {
		this.jobLauncher = jobLauncher;
		this.batchRunGuard = batchRunGuard;
		this.clock = clock;
		this.rssFeedCollectionJob = rssFeedCollectionJob;
	}

	/**
	 * 승인된 RSS/Atom 소스 전체 수집 job을 실행한다.
	 *
	 * @param requestedBy 실행 주체
	 * @param reason 실행 사유
	 * @return Batch job execution
	 */
	public JobExecution runRssFeedCollection(String requestedBy, String reason) {
		return runRssFeedCollection(null, requestedBy, reason);
	}

	/**
	 * 승인된 RSS/Atom 소스 수집 job을 실행한다.
	 *
	 * @param sourceId 단일 수집 source ID. null이면 전체 승인 source 수집
	 * @param requestedBy 실행 주체
	 * @param reason 실행 사유
	 * @return Batch job execution
	 */
	public JobExecution runRssFeedCollection(Long sourceId, String requestedBy, String reason) {
		batchRunGuard.assertNotRunning(BatchJobNames.RSS_FEED_COLLECTION_JOB);
		JobParametersBuilder parametersBuilder = new JobParametersBuilder()
			.addString("requestedAt", clock.instant().toString())
			.addString("requestedBy", normalizeParameter(requestedBy, "unknown"))
			.addString("reason", normalizeParameter(reason, "unspecified"));
		if (sourceId != null) {
			parametersBuilder.addLong("sourceId", sourceId);
		}
		JobParameters jobParameters = parametersBuilder.toJobParameters();

		try {
			return jobLauncher.run(rssFeedCollectionJob, jobParameters);
		} catch (Exception exception) {
			throw new BatchJobLaunchException(BatchJobNames.RSS_FEED_COLLECTION_JOB, exception);
		}
	}

	private static String normalizeParameter(String value, String defaultValue) {
		String normalized = StringNormalizer.normalizeWhitespaceToNull(value);
		if (normalized == null) {
			return defaultValue;
		}
		return StringNormalizer.truncate(normalized, PARAMETER_MAX_LENGTH);
	}
}

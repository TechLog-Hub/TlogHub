package com.techloghub.api.worker.batch;

import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 같은 Batch job의 중복 실행을 차단한다.
 */
@Component
@RequiredArgsConstructor
public class BatchRunGuard {

	private final JobExplorer jobExplorer;

	/**
	 * 실행 중인 동일 job이 없을 때만 통과한다.
	 *
	 * @param jobName 검사할 job 이름
	 */
	public void assertNotRunning(String jobName) {
		if (!jobExplorer.findRunningJobExecutions(jobName).isEmpty()) {
			throw new BatchJobAlreadyRunningException(jobName);
		}
	}
}

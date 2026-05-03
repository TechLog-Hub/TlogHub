package com.techloghub.api.worker.batch;

/**
 * Batch job 실행 요청이 Spring Batch 단계에서 실패했을 때 사용하는 worker 내부 예외다.
 */
public class BatchJobLaunchException extends RuntimeException {

	public BatchJobLaunchException(String jobName, Throwable cause) {
		super("failed to launch batch job: " + jobName, cause);
	}
}

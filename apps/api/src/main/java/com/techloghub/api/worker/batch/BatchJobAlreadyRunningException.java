package com.techloghub.api.worker.batch;

/**
 * 동일 Batch job이 이미 실행 중일 때 사용하는 worker 내부 예외다.
 */
public class BatchJobAlreadyRunningException extends RuntimeException {

	public BatchJobAlreadyRunningException(String jobName) {
		super("batch job is already running: " + jobName);
	}
}

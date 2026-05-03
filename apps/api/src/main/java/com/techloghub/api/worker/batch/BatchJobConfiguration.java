package com.techloghub.api.worker.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.techloghub.api.worker.collection.FeedCollectionService;

/**
 * RSS 수집 worker의 Spring Batch Job/Step bean을 정의한다.
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(TechlogBatchProperties.class)
public class BatchJobConfiguration {

	private static final int JOB_EXECUTOR_CORE_POOL_SIZE = 2;
	private static final int JOB_EXECUTOR_MAX_POOL_SIZE = 4;
	private static final int JOB_EXECUTOR_QUEUE_CAPACITY = 20;

	@Bean
	public TaskExecutor batchJobTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setThreadNamePrefix("techlog-batch-");
		taskExecutor.setCorePoolSize(JOB_EXECUTOR_CORE_POOL_SIZE);
		taskExecutor.setMaxPoolSize(JOB_EXECUTOR_MAX_POOL_SIZE);
		taskExecutor.setQueueCapacity(JOB_EXECUTOR_QUEUE_CAPACITY);
		return taskExecutor;
	}

	@Primary
	@Bean(name = "asyncBatchJobLauncher")
	public JobLauncher asyncBatchJobLauncher(
		JobRepository jobRepository,
		@Qualifier("batchJobTaskExecutor") TaskExecutor taskExecutor
	) throws Exception {
		TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
		jobLauncher.setJobRepository(jobRepository);
		jobLauncher.setTaskExecutor(taskExecutor);
		jobLauncher.afterPropertiesSet();
		return jobLauncher;
	}

	@Bean(name = BatchJobNames.RSS_FEED_COLLECTION_JOB)
	public Job rssFeedCollectionJob(JobRepository jobRepository, Step collectFeedsStep) {
		return new JobBuilder(BatchJobNames.RSS_FEED_COLLECTION_JOB, jobRepository)
			.start(collectFeedsStep)
			.build();
	}

	@Bean(name = BatchJobNames.COLLECT_FEEDS_STEP)
	public Step collectFeedsStep(
		JobRepository jobRepository,
		FeedCollectionService feedCollectionService
	) {
		return new StepBuilder(BatchJobNames.COLLECT_FEEDS_STEP, jobRepository)
			.tasklet((contribution, chunkContext) -> {
				Long sourceId = contribution.getStepExecution()
					.getJobExecution()
					.getJobParameters()
					.getLong("sourceId");
				if (sourceId == null) {
					feedCollectionService.collectApprovedSources();
				} else {
					feedCollectionService.collectSource(sourceId);
				}
				return RepeatStatus.FINISHED;
			}, new ResourcelessTransactionManager())
			.build();
	}
}

package com.techloghub.api.worker.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import com.techloghub.api.worker.collection.FeedCollectionService;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * RSS 수집 worker의 Spring Batch Job/Step bean을 정의한다.
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@EnableConfigurationProperties(TechlogBatchProperties.class)
public class BatchJobConfiguration {

	@Bean(name = BatchJobNames.RSS_FEED_COLLECTION_JOB)
	public Job rssFeedCollectionJob(JobRepository jobRepository, Step collectFeedsStep) {
		return new JobBuilder(BatchJobNames.RSS_FEED_COLLECTION_JOB, jobRepository)
			.start(collectFeedsStep)
			.build();
	}

	@Bean(name = BatchJobNames.COLLECT_FEEDS_STEP)
	public Step collectFeedsStep(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
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
			}, transactionManager)
			.build();
	}
}

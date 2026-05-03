package com.techloghub.api.worker.batch;

import static com.techloghub.api.testsupport.TestTags.INTEGRATION;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import com.techloghub.api.TechlogHubApiApplication;
import com.techloghub.api.content.domain.Company;
import com.techloghub.api.content.domain.SourceBlog;
import com.techloghub.api.content.domain.SourceType;
import com.techloghub.api.content.repository.ArchivedPostRepository;
import com.techloghub.api.content.repository.CollectionRunRepository;
import com.techloghub.api.content.repository.CompanyRepository;
import com.techloghub.api.content.repository.PostSourceOccurrenceRepository;
import com.techloghub.api.content.repository.SourceBlogRepository;
import com.techloghub.api.worker.collection.FeedClient;
import com.techloghub.api.worker.collection.FeedEntryCandidate;

@ActiveProfiles("test")
@SpringBootTest(
	classes = {
		TechlogHubApiApplication.class,
		BatchJobLauncherServiceIntegrationTests.FeedClientTestConfiguration.class
	},
	webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@Tag(INTEGRATION)
class BatchJobLauncherServiceIntegrationTests {

	private static final String FEED_URL = "https://toss.tech/rss.xml";

	@Autowired
	private BatchJobLauncherService batchJobLauncherService;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private JobExplorer jobExplorer;

	@Autowired
	private StubFeedClient stubFeedClient;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private SourceBlogRepository sourceBlogRepository;

	@Autowired
	private ArchivedPostRepository archivedPostRepository;

	@Autowired
	private PostSourceOccurrenceRepository postSourceOccurrenceRepository;

	@Autowired
	private CollectionRunRepository collectionRunRepository;

	@BeforeEach
	void setUp() {
		clearData();
	}

	@AfterEach
	void tearDown() {
		clearData();
	}

	private void clearData() {
		collectionRunRepository.deleteAll();
		postSourceOccurrenceRepository.deleteAll();
		archivedPostRepository.deleteAll();
		sourceBlogRepository.deleteAll();
		companyRepository.deleteAll();
		stubFeedClient.clear();
	}

	@Test
	@DisplayName("수동 RSS 수집 Batch job을 실행하면 승인 source를 수집하고 COMPLETED 상태로 종료한다")
	void runRssFeedCollection_approvedSource_completesJob() throws Exception {
		sourceBlogRepository.saveAndFlush(approvedSource());
		stubFeedClient.success(FEED_URL, List.of(new FeedEntryCandidate(
			"Spring Batch 운영",
			"https://toss.tech/batch",
			"https://toss.tech/batch",
			Instant.parse("2026-05-02T09:30:00Z"),
			"summary"
		)));

		JobExecution jobExecution = batchJobLauncherService.runRssFeedCollection("test", "manual");
		JobExecution completedJobExecution = awaitCompletion(jobExecution.getId());

		assertThat(completedJobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(archivedPostRepository.count()).isEqualTo(1);
		assertThat(collectionRunRepository.count()).isEqualTo(1);
	}

	@Test
	@DisplayName("기본 설정에서는 scheduler bean이 생성되지 않는다")
	void context_defaultProperties_doesNotCreateScheduledBatchRunner() {
		assertThat(applicationContext.getBeansOfType(ScheduledBatchRunner.class)).isEmpty();
	}

	private SourceBlog approvedSource() {
		Company company = companyRepository.saveAndFlush(Company.create("toss", "토스", "Toss"));
		SourceBlog sourceBlog = SourceBlog.propose(
			company,
			"토스 기술 블로그",
			"https://toss.tech",
			FEED_URL,
			SourceType.RSS
		);
		sourceBlog.approve();
		return sourceBlog;
	}

	private JobExecution awaitCompletion(Long executionId) throws Exception {
		for (int attempt = 0; attempt < 100; attempt++) {
			JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
			if (jobExecution != null && !jobExecution.getStatus().isRunning()) {
				return jobExecution;
			}
			Thread.sleep(50);
		}
		throw new AssertionError("batch job did not finish: " + executionId);
	}

	@TestConfiguration
	static class FeedClientTestConfiguration {

		@Bean
		@Primary
		StubFeedClient stubFeedClient() {
			return new StubFeedClient();
		}
	}

	static class StubFeedClient implements FeedClient {

		private final Map<String, List<FeedEntryCandidate>> successes = new ConcurrentHashMap<>();

		void success(String feedUrl, List<FeedEntryCandidate> candidates) {
			successes.put(feedUrl, candidates);
		}

		void clear() {
			successes.clear();
		}

		@Override
		public List<FeedEntryCandidate> fetch(String feedUrl) {
			return successes.getOrDefault(feedUrl, List.of());
		}
	}
}

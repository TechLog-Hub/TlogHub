package com.techloghub.api.admin.api;

import static com.techloghub.api.testsupport.TestTags.INTEGRATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techloghub.api.TechlogHubApiApplication;
import com.techloghub.api.admin.domain.AdminUser;
import com.techloghub.api.admin.repository.AdminAuditLogRepository;
import com.techloghub.api.admin.repository.AdminUserRepository;
import com.techloghub.api.content.domain.CollectionRunStatus;
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
import com.techloghub.api.worker.collection.FeedFetchException;

@ActiveProfiles("test")
@SpringBootTest(
	classes = {
		TechlogHubApiApplication.class,
		AdminJobApiIntegrationTests.FeedClientTestConfiguration.class
	}
)
@AutoConfigureMockMvc
@Tag(INTEGRATION)
class AdminJobApiIntegrationTests {

	private static final String ADMIN_EMAIL = "admin-job@techloghub.local";
	private static final String ADMIN_PASSWORD = "admin1234";
	private static final String FEED_URL = "https://toss.tech/rss.xml";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JobExplorer jobExplorer;

	@Autowired
	private BCryptPasswordEncoder adminPasswordEncoder;

	@Autowired
	private StubFeedClient stubFeedClient;

	@Autowired
	private AdminAuditLogRepository adminAuditLogRepository;

	@Autowired
	private AdminUserRepository adminUserRepository;

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
		adminAuditLogRepository.deleteAll();
		adminUserRepository.deleteAll();
		collectionRunRepository.deleteAll();
		postSourceOccurrenceRepository.deleteAll();
		archivedPostRepository.deleteAll();
		sourceBlogRepository.deleteAll();
		companyRepository.deleteAll();
		stubFeedClient.clear();
	}

	@Test
	@DisplayName("관리자가 RSS 수집 job을 수동 실행하면 Batch 실행 정보와 저장 결과를 반환한다")
	void runCollectionJob_adminRequest_returnsJobExecution() throws Exception {
		String accessToken = createAdminAndLogin();
		SourceBlog sourceBlog = sourceBlogRepository.saveAndFlush(approvedSource());
		stubFeedClient.success(FEED_URL, List.of(new FeedEntryCandidate(
			"Spring Batch 운영",
			"https://toss.tech/batch",
			"https://toss.tech/batch",
			Instant.parse("2026-05-02T09:30:00Z"),
			"summary"
		)));

		String runResponse = mockMvc.perform(post("/api/v1/admin/jobs/collect/run")
				.header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
				.param("sourceId", sourceBlog.getId().toString())
				.param("reason", "manual-test"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.jobName").value("rssFeedCollectionJob"))
			.andExpect(jsonPath("$.executionId").isNumber())
			.andExpect(jsonPath("$.status").value(Matchers.anyOf(
				Matchers.is("starting"),
				Matchers.is("started"),
				Matchers.is("completed")
			)))
			.andExpect(jsonPath("$.requestedAt").isNotEmpty())
			.andReturn()
			.getResponse()
			.getContentAsString();
		awaitCompletion(readLong(runResponse, "executionId"));

		assertThat(archivedPostRepository.count()).isEqualTo(1);
		assertThat(collectionRunRepository.findByStatusOrderByStartedAtAsc(CollectionRunStatus.SUCCESS)).hasSize(1);
	}

	@Test
	@DisplayName("관리자는 최근 RSS 수집 실패 이력을 조회할 수 있다")
	void getCollectionFailures_failedCollection_returnsFailures() throws Exception {
		String accessToken = createAdminAndLogin();
		sourceBlogRepository.saveAndFlush(approvedSource());
		stubFeedClient.failure(FEED_URL, new FeedFetchException("timeout"));
		String runResponse = mockMvc.perform(post("/api/v1/admin/jobs/collect/run")
				.header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		awaitCompletion(readLong(runResponse, "executionId"));

		mockMvc.perform(get("/api/v1/admin/jobs/failures")
				.header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].sourceName").value("토스 기술 블로그"))
			.andExpect(jsonPath("$[0].status").value("failed"))
			.andExpect(jsonPath("$[0].failureReason").value(Matchers.containsString("FeedFetchException")));
	}

	private String createAdminAndLogin() throws Exception {
		adminUserRepository.saveAndFlush(AdminUser.create(
			ADMIN_EMAIL,
			adminPasswordEncoder.encode(ADMIN_PASSWORD)
		));
		String loginResponse = mockMvc.perform(post("/api/v1/admin/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "admin-job@techloghub.local",
					  "password": "admin1234"
					}
					"""))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		JsonNode node = objectMapper.readTree(loginResponse);
		return node.get("accessToken").asText();
	}

	private Long readLong(String json, String fieldName) throws Exception {
		JsonNode node = objectMapper.readTree(json);
		return node.get(fieldName).asLong();
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

	private SourceBlog approvedSource() {
		Company company = companyRepository.saveAndFlush(Company.create("toss-job", "토스", "Toss"));
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

	private String bearer(String token) {
		return "Bearer " + token;
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
		private final Map<String, RuntimeException> failures = new ConcurrentHashMap<>();

		void success(String feedUrl, List<FeedEntryCandidate> candidates) {
			successes.put(feedUrl, candidates);
		}

		void failure(String feedUrl, RuntimeException exception) {
			failures.put(feedUrl, exception);
		}

		void clear() {
			successes.clear();
			failures.clear();
		}

		@Override
		public List<FeedEntryCandidate> fetch(String feedUrl) {
			RuntimeException failure = failures.get(feedUrl);
			if (failure != null) {
				throw failure;
			}
			return successes.getOrDefault(feedUrl, List.of());
		}
	}
}

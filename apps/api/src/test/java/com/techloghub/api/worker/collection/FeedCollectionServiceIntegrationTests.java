package com.techloghub.api.worker.collection;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import com.techloghub.api.TechlogHubApiApplication;
import com.techloghub.api.content.domain.CollectionRunStatus;
import com.techloghub.api.content.domain.Company;
import com.techloghub.api.content.domain.SourceBlog;
import com.techloghub.api.content.domain.SourceType;
import com.techloghub.api.content.domain.SummaryState;
import com.techloghub.api.content.domain.VisibilityState;
import com.techloghub.api.content.repository.AiSummaryRepository;
import com.techloghub.api.content.repository.ArchivedPostRepository;
import com.techloghub.api.content.repository.CollectionRunRepository;
import com.techloghub.api.content.repository.CompanyRepository;
import com.techloghub.api.content.repository.FeedEntrySnapshotRepository;
import com.techloghub.api.content.repository.PostSourceOccurrenceRepository;
import com.techloghub.api.content.repository.SourceBlogRepository;

@ActiveProfiles("test")
@SpringBootTest(
	classes = {
		TechlogHubApiApplication.class,
		FeedCollectionServiceIntegrationTests.FeedClientTestConfiguration.class
	},
	webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@Tag(INTEGRATION)
class FeedCollectionServiceIntegrationTests {

	private static final String FEED_URL = "https://toss.tech/rss.xml";
	private static final Instant PUBLISHED_AT = Instant.parse("2026-05-02T09:30:00Z");

	@Autowired
	private FeedCollectionService feedCollectionService;

	@Autowired
	private StubFeedClient stubFeedClient;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private SourceBlogRepository sourceBlogRepository;

	@Autowired
	private ArchivedPostRepository archivedPostRepository;

	@Autowired
	private AiSummaryRepository aiSummaryRepository;

	@Autowired
	private FeedEntrySnapshotRepository feedEntrySnapshotRepository;

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
		feedEntrySnapshotRepository.deleteAll();
		collectionRunRepository.deleteAll();
		aiSummaryRepository.deleteAll();
		postSourceOccurrenceRepository.deleteAll();
		archivedPostRepository.deleteAll();
		sourceBlogRepository.deleteAll();
		companyRepository.deleteAll();
		stubFeedClient.clear();
	}

	@Test
	@DisplayName("승인된 RSS source를 수집하면 신규 글과 발생 이력을 저장하고 재실행 시 중복 글을 만들지 않는다")
	void collectApprovedSources_approvedRssSource_savesNewPostsIdempotently() {
		SourceBlog sourceBlog = sourceBlogRepository.saveAndFlush(approvedSource("toss", FEED_URL));
		stubFeedClient.success(FEED_URL, List.of(
			candidate("Spring Boot 운영 경험", "https://toss.tech/spring", PUBLISHED_AT),
			candidate("Kafka lag 대응", "https://toss.tech/kafka", PUBLISHED_AT.plusSeconds(300))
		));

		FeedCollectionSummary firstSummary = feedCollectionService.collectApprovedSources();
		FeedCollectionSummary secondSummary = feedCollectionService.collectApprovedSources();

		assertThat(firstSummary.sourceCount()).isEqualTo(1);
		assertThat(firstSummary.successCount()).isEqualTo(1);
		assertThat(firstSummary.newPostCount()).isEqualTo(2);
		assertThat(secondSummary.newPostCount()).isZero();
		assertThat(secondSummary.duplicateCount()).isEqualTo(2);
		assertThat(archivedPostRepository.count()).isEqualTo(2);
		assertThat(aiSummaryRepository.count()).isEqualTo(2);
		assertThat(feedEntrySnapshotRepository.count()).isEqualTo(2);
		assertThat(postSourceOccurrenceRepository.count()).isEqualTo(2);
		assertThat(collectionRunRepository.findByStatusOrderByStartedAtAsc(CollectionRunStatus.SUCCESS)).hasSize(2);
		assertThat(archivedPostRepository.findAll())
			.allSatisfy(post -> {
				assertThat(post.getVisibilityState()).isEqualTo(VisibilityState.PUBLISHED);
				assertThat(aiSummaryRepository.findByArchivedPost_IdAndCurrentTrue(post.getId()))
					.hasValueSatisfying(summary -> assertThat(summary.getSummaryState()).isEqualTo(SummaryState.PENDING));
			});
		assertThat(feedEntrySnapshotRepository.findAll())
			.allSatisfy(snapshot -> assertThat(snapshot.getSummaryText()).isEqualTo("summary"));
		assertThat(sourceBlogRepository.findById(sourceBlog.getId()))
			.hasValueSatisfying(found -> assertThat(found.getLastCollectedAt()).isNotNull());
	}

	@Test
	@DisplayName("feed 조회가 실패하면 source별 CollectionRun을 FAILED로 기록하고 다른 저장을 수행하지 않는다")
	void collectApprovedSources_feedFailure_recordsFailedRun() {
		sourceBlogRepository.saveAndFlush(approvedSource("naver", FEED_URL));
		stubFeedClient.failure(FEED_URL, new FeedFetchException("timeout"));

		FeedCollectionSummary summary = feedCollectionService.collectApprovedSources();

		assertThat(summary.sourceCount()).isEqualTo(1);
		assertThat(summary.failureCount()).isEqualTo(1);
		assertThat(archivedPostRepository.count()).isZero();
		assertThat(collectionRunRepository.findByStatusOrderByStartedAtAsc(CollectionRunStatus.FAILED))
			.hasSize(1)
			.first()
			.satisfies(run -> assertThat(run.getFailureReason()).contains("FeedFetchException"));
	}

	private Company createCompany(String slug) {
		return companyRepository.saveAndFlush(Company.create(slug, slug + " ko", slug + " en"));
	}

	private SourceBlog approvedSource(String companySlug, String feedUrl) {
		Company company = createCompany(companySlug);
		SourceBlog sourceBlog = SourceBlog.propose(
			company,
			companySlug + " 기술 블로그",
			"https://" + companySlug + ".tech",
			feedUrl,
			SourceType.RSS
		);
		sourceBlog.approve();
		return sourceBlog;
	}

	private static FeedEntryCandidate candidate(String title, String url, Instant publishedAt) {
		return new FeedEntryCandidate(title, url, url, publishedAt, "summary");
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

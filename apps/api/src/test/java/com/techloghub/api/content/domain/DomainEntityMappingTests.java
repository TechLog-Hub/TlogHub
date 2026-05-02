package com.techloghub.api.content.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.techloghub.api.admin.domain.AdminAuditLog;
import com.techloghub.api.admin.domain.AdminUser;
import com.techloghub.api.common.config.JpaAuditingConfiguration;
import com.techloghub.api.subscription.domain.CompanySubscription;
import com.techloghub.api.subscription.domain.NotificationEvent;
import com.techloghub.api.subscription.domain.Subscriber;
import com.techloghub.api.subscription.domain.SubscriptionVerificationRequest;

@ActiveProfiles("test")
@DataJpaTest
@Import(JpaAuditingConfiguration.class)
class DomainEntityMappingTests {

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void persistsCoreDomainGraph() {
		Instant now = Instant.parse("2026-05-02T10:00:00Z");

		Company company = Company.create("toss", "토스", "Toss");
		entityManager.persist(company);

		SourceBlog sourceBlog = SourceBlog.propose(
			company,
			"토스 기술 블로그",
			"https://toss.tech",
			"https://toss.tech/rss.xml",
			SourceType.RSS
		);
		sourceBlog.approve();
		entityManager.persist(sourceBlog);

		JobCategory backend = JobCategory.create("Backend", "백엔드", 1);
		TopicTag spring = TopicTag.create("spring", "Spring", "spring");
		entityManager.persist(backend);
		entityManager.persist(spring);

		ArchivedPost post = ArchivedPost.collect(
			sourceBlog,
			"toss-spring-boot",
			"Spring Boot 운영 경험",
			"https://toss.tech/article/spring-boot",
			"https://toss.tech/article/spring-boot",
			"fingerprint-toss-spring-boot",
			now.minus(1, ChronoUnit.HOURS),
			now
		);
		post.replaceJobCategories(List.of(backend));
		post.replaceTopicTags(List.of(spring));
		post.markClassified();
		post.publish();
		entityManager.persistAndFlush(post);

		PostSourceOccurrence occurrence = PostSourceOccurrence.original(
			post,
			sourceBlog,
			"https://toss.tech/article/spring-boot",
			now.minus(1, ChronoUnit.HOURS)
		);
		AiSummary summary = AiSummary.ready(
			post,
			1,
			"Spring Boot 운영 경험을 정리한 글",
			"[\"운영 자동화\", \"장애 대응\"]",
			"gpt-4.1-mini",
			"summary-v1",
			now
		);
		CollectionRun collectionRun = CollectionRun.start(sourceBlog, now.minus(5, ChronoUnit.MINUTES));
		collectionRun.succeed(now, 1, 1);
		entityManager.persist(occurrence);
		entityManager.persist(summary);
		entityManager.persist(collectionRun);

		Subscriber subscriber = Subscriber.pending("dev@example.com");
		subscriber.activate("manage-token-hash", now);
		entityManager.persist(subscriber);
		entityManager.persist(SubscriptionVerificationRequest.issue(subscriber, "verify-token-hash", now.plus(1, ChronoUnit.DAYS)));
		entityManager.persist(CompanySubscription.subscribe(subscriber, company));
		entityManager.persist(NotificationEvent.requestEmail(subscriber, post, now));

		AdminUser adminUser = AdminUser.create("admin@techloghub.local", "{noop}admin1234");
		entityManager.persistAndFlush(adminUser);
		entityManager.persist(AdminAuditLog.record(adminUser, "POST_PUBLISH", "archived_post", post.getId(), "{}", "{}"));

		entityManager.flush();
		entityManager.clear();

		ArchivedPost persistedPost = entityManager.find(ArchivedPost.class, post.getId());
		assertThat(persistedPost.getVisibilityState()).isEqualTo(VisibilityState.PUBLISHED);
		assertThat(persistedPost.getJobCategories()).hasSize(1);
		assertThat(entityManager.find(AiSummary.class, summary.getId()).isCurrent()).isTrue();
		assertThat(entityManager.find(Subscriber.class, subscriber.getId()).getManageTokenHash()).isEqualTo("manage-token-hash");
	}

	@Test
	void blocksPublishingWhenReviewIsRequired() {
		Company company = Company.create("naver", "네이버", "Naver");
		SourceBlog sourceBlog = SourceBlog.propose(
			company,
			"네이버 D2",
			"https://d2.naver.com",
			"https://d2.naver.com/rss",
			SourceType.RSS
		);
		ArchivedPost post = ArchivedPost.collect(
			sourceBlog,
			"naver-d2-kafka",
			"Kafka 운영",
			"https://d2.naver.com/kafka",
			"https://d2.naver.com/kafka",
			"fingerprint-naver-d2-kafka",
			Instant.now(),
			Instant.now()
		);

		post.requestReview("near duplicate candidate");

		assertThatThrownBy(post::publish)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("review required");
	}
}

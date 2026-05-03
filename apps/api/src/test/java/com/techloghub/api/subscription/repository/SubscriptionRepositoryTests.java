package com.techloghub.api.subscription.repository;

import static com.techloghub.api.testsupport.TestTags.INTEGRATION;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.techloghub.api.common.config.JpaAuditingConfiguration;
import com.techloghub.api.common.config.QueryDslConfiguration;
import com.techloghub.api.content.domain.ArchivedPost;
import com.techloghub.api.content.domain.Company;
import com.techloghub.api.content.domain.SourceBlog;
import com.techloghub.api.content.domain.SourceType;
import com.techloghub.api.content.repository.ArchivedPostRepository;
import com.techloghub.api.content.repository.CompanyRepository;
import com.techloghub.api.content.repository.SourceBlogRepository;
import com.techloghub.api.subscription.domain.CompanySubscription;
import com.techloghub.api.subscription.domain.NotificationEvent;
import com.techloghub.api.subscription.domain.NotificationStatus;
import com.techloghub.api.subscription.domain.Subscriber;
import com.techloghub.api.subscription.domain.SubscriptionVerificationRequest;

@ActiveProfiles("test")
@DataJpaTest
@Import({JpaAuditingConfiguration.class, QueryDslConfiguration.class})
@Tag(INTEGRATION)
class SubscriptionRepositoryTests {

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private SourceBlogRepository sourceBlogRepository;

	@Autowired
	private ArchivedPostRepository archivedPostRepository;

	@Autowired
	private SubscriberRepository subscriberRepository;

	@Autowired
	private SubscriptionVerificationRequestRepository verificationRequestRepository;

	@Autowired
	private CompanySubscriptionRepository companySubscriptionRepository;

	@Autowired
	private NotificationEventRepository notificationEventRepository;

	@Test
	void findsSubscriberVerificationSubscriptionAndNotification() {
		Instant now = Instant.parse("2026-05-02T12:00:00Z");
		Company company = companyRepository.save(Company.create("kakao", "카카오", "Kakao"));
		ArchivedPost post = archivedPostRepository.saveAndFlush(publishedPost(company, now));
		Subscriber subscriber = Subscriber.pending("DEV@EXAMPLE.COM");
		subscriber.activate("manage-token-hash", now);
		subscriberRepository.saveAndFlush(subscriber);
		verificationRequestRepository.saveAndFlush(SubscriptionVerificationRequest.issue(
			subscriber,
			"verify-token-hash",
			now.plus(1, ChronoUnit.DAYS)
		));
		companySubscriptionRepository.saveAndFlush(CompanySubscription.subscribe(subscriber, company));
		notificationEventRepository.saveAndFlush(NotificationEvent.requestEmail(subscriber, post, now));

		assertThat(subscriberRepository.findByEmail("dev@example.com")).isPresent();
		assertThat(subscriberRepository.existsByEmail("dev@example.com")).isTrue();
		assertThat(subscriberRepository.findByManageTokenHash("manage-token-hash")).isPresent();
		assertThat(verificationRequestRepository.findByTokenHash("verify-token-hash")).isPresent();
		assertThat(verificationRequestRepository.findBySubscriber_IdAndUsedAtIsNull(subscriber.getId())).hasSize(1);
		assertThat(companySubscriptionRepository.findBySubscriber_IdAndCompany_Slug(subscriber.getId(), "kakao")).isPresent();
		assertThat(companySubscriptionRepository.findBySubscriber_IdAndActiveTrueOrderByCompany_NameKoAsc(subscriber.getId()))
			.hasSize(1);
		assertThat(companySubscriptionRepository.existsBySubscriber_IdAndCompany_IdAndActiveTrue(
			subscriber.getId(),
			company.getId()
		)).isTrue();
		assertThat(notificationEventRepository.existsBySubscriber_IdAndArchivedPost_Id(subscriber.getId(), post.getId()))
			.isTrue();
		assertThat(notificationEventRepository.findTop100ByStatusOrderByRequestedAtAsc(NotificationStatus.REQUESTED))
			.hasSize(1);
	}

	private ArchivedPost publishedPost(Company company, Instant now) {
		SourceBlog sourceBlog = SourceBlog.propose(
			company,
			"카카오 기술 블로그",
			"https://tech.kakao.com",
			"https://tech.kakao.com/rss.xml",
			SourceType.RSS
		);
		sourceBlog.approve();
		sourceBlogRepository.saveAndFlush(sourceBlog);
		ArchivedPost post = ArchivedPost.collect(
			sourceBlog,
			"kakao-repository",
			"Repository 설계",
			"https://tech.kakao.com/repository",
			"https://tech.kakao.com/repository",
			"fingerprint-kakao-repository",
			now.minus(1, ChronoUnit.HOURS),
			now
		);
		post.markClassified();
		post.publish();
		return post;
	}
}

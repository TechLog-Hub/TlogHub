package com.techloghub.api.content.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.techloghub.api.common.config.JpaAuditingConfiguration;
import com.techloghub.api.content.domain.AiSummary;
import com.techloghub.api.content.domain.ArchivedPost;
import com.techloghub.api.content.domain.CollectionRun;
import com.techloghub.api.content.domain.CollectionRunStatus;
import com.techloghub.api.content.domain.Company;
import com.techloghub.api.content.domain.CompanyStatus;
import com.techloghub.api.content.domain.JobCategory;
import com.techloghub.api.content.domain.PostSourceOccurrence;
import com.techloghub.api.content.domain.SourceBlog;
import com.techloghub.api.content.domain.SourceBlogStatus;
import com.techloghub.api.content.domain.SourceType;
import com.techloghub.api.content.domain.TopicTag;
import com.techloghub.api.content.domain.VisibilityState;

@ActiveProfiles("test")
@DataJpaTest
@Import(JpaAuditingConfiguration.class)
class ContentRepositoryTests {

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private SourceBlogRepository sourceBlogRepository;

	@Autowired
	private JobCategoryRepository jobCategoryRepository;

	@Autowired
	private TopicTagRepository topicTagRepository;

	@Autowired
	private ArchivedPostRepository archivedPostRepository;

	@Autowired
	private AiSummaryRepository aiSummaryRepository;

	@Autowired
	private PostSourceOccurrenceRepository postSourceOccurrenceRepository;

	@Autowired
	private CollectionRunRepository collectionRunRepository;

	@Test
	void findsPublishedPostWithTaxonomyAndSummary() {
		Instant now = Instant.parse("2026-05-02T12:00:00Z");
		Company company = companyRepository.save(Company.create("toss", "토스", "Toss"));
		SourceBlog sourceBlog = sourceBlogRepository.save(approvedSource(company));
		JobCategory backend = jobCategoryRepository.save(JobCategory.create("Backend", "백엔드", 1));
		TopicTag spring = topicTagRepository.save(TopicTag.create("spring", "Spring", "spring"));

		ArchivedPost post = ArchivedPost.collect(
			sourceBlog,
			"toss-spring",
			"Spring 운영 경험",
			"https://toss.tech/spring",
			"https://toss.tech/spring",
			"fingerprint-toss-spring",
			now.minus(1, ChronoUnit.HOURS),
			now
		);
		post.replaceJobCategories(List.of(backend));
		post.replaceTopicTags(List.of(spring));
		post.markClassified();
		post.publish();
		archivedPostRepository.saveAndFlush(post);
		aiSummaryRepository.saveAndFlush(AiSummary.ready(
			post,
			1,
			"Spring 운영 경험 요약",
			"[\"운영\", \"장애 대응\"]",
			"gpt-4.1-mini",
			"summary-v1",
			now
		));
		postSourceOccurrenceRepository.saveAndFlush(PostSourceOccurrence.original(
			post,
			sourceBlog,
			"https://toss.tech/spring",
			now.minus(1, ChronoUnit.HOURS)
		));

		assertThat(companyRepository.existsBySlug("toss")).isTrue();
		assertThat(companyRepository.findByStatusOrderByNameKoAsc(CompanyStatus.ACTIVE)).hasSize(1);
		assertThat(sourceBlogRepository.findByStatusOrderByIdAsc(SourceBlogStatus.APPROVED)).hasSize(1);
		assertThat(jobCategoryRepository.findByActiveTrueOrderByDisplayOrderAsc()).containsExactly(backend);
		assertThat(topicTagRepository.findByActiveTrueOrderByLabelAsc()).containsExactly(spring);
		assertThat(archivedPostRepository.existsByCanonicalFingerprint("fingerprint-toss-spring")).isTrue();
		assertThat(archivedPostRepository.findByVisibilityStateOrderByPublishedAtDesc(
			VisibilityState.PUBLISHED,
			PageRequest.of(0, 10)
		)).hasSize(1);
		assertThat(archivedPostRepository.findByCompany_SlugAndVisibilityStateOrderByPublishedAtDesc(
			"toss",
			VisibilityState.PUBLISHED,
			PageRequest.of(0, 10)
		)).hasSize(1);
		assertThat(archivedPostRepository.findDistinctByJobCategories_CodeAndVisibilityStateOrderByPublishedAtDesc(
			"Backend",
			VisibilityState.PUBLISHED,
			PageRequest.of(0, 10)
		)).hasSize(1);
		assertThat(archivedPostRepository.findDistinctByTopicTags_SlugAndVisibilityStateOrderByPublishedAtDesc(
			"spring",
			VisibilityState.PUBLISHED,
			PageRequest.of(0, 10)
		)).hasSize(1);
		assertThat(archivedPostRepository.findDetailedBySlugAndVisibilityState("toss-spring", VisibilityState.PUBLISHED))
			.hasValueSatisfying(found -> {
				assertThat(found.getCompany().getSlug()).isEqualTo("toss");
				assertThat(found.getJobCategories()).extracting(JobCategory::getCode).containsExactly("Backend");
				assertThat(found.getTopicTags()).extracting(TopicTag::getSlug).containsExactly("spring");
			});
		assertThat(aiSummaryRepository.findByArchivedPost_IdAndCurrentTrue(post.getId())).isPresent();
		assertThat(aiSummaryRepository.findByArchivedPost_IdOrderBySummaryVersionDesc(post.getId())).hasSize(1);
		assertThat(postSourceOccurrenceRepository.existsBySourceBlog_IdAndOriginUrl(sourceBlog.getId(), "https://toss.tech/spring"))
			.isTrue();
		assertThat(postSourceOccurrenceRepository.findByArchivedPost_IdOrderByPublishedAtDesc(post.getId())).hasSize(1);
	}

	@Test
	void findsCollectionRunsBySourceAndStatus() {
		Instant now = Instant.parse("2026-05-02T12:00:00Z");
		Company company = companyRepository.save(Company.create("naver", "네이버", "Naver"));
		SourceBlog sourceBlog = sourceBlogRepository.save(approvedSource(company));
		CollectionRun run = CollectionRun.start(sourceBlog, now.minus(5, ChronoUnit.MINUTES));
		run.succeed(now, 1, 1);
		collectionRunRepository.saveAndFlush(run);

		assertThat(collectionRunRepository.findTop20BySourceBlog_IdOrderByStartedAtDesc(sourceBlog.getId()))
			.containsExactly(run);
		assertThat(collectionRunRepository.findByStatusOrderByStartedAtAsc(CollectionRunStatus.SUCCESS))
			.containsExactly(run);
	}

	private static SourceBlog approvedSource(Company company) {
		SourceBlog sourceBlog = SourceBlog.propose(
			company,
			company.getNameKo() + " 기술 블로그",
			"https://" + company.getSlug() + ".tech",
			"https://" + company.getSlug() + ".tech/rss.xml",
			SourceType.RSS
		);
		sourceBlog.approve();
		return sourceBlog;
	}
}

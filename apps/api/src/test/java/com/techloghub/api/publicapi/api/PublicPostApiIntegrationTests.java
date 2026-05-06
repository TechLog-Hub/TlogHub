package com.techloghub.api.publicapi.api;

import static com.techloghub.api.testsupport.TestTags.INTEGRATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.techloghub.api.content.domain.AiSummary;
import com.techloghub.api.content.domain.ArchivedPost;
import com.techloghub.api.content.domain.Company;
import com.techloghub.api.content.domain.JobCategory;
import com.techloghub.api.content.domain.SourceBlog;
import com.techloghub.api.content.domain.SourceType;
import com.techloghub.api.content.domain.TopicTag;
import com.techloghub.api.content.repository.AiSummaryRepository;
import com.techloghub.api.content.repository.ArchivedPostRepository;
import com.techloghub.api.content.repository.CompanyRepository;
import com.techloghub.api.content.repository.JobCategoryRepository;
import com.techloghub.api.content.repository.SourceBlogRepository;
import com.techloghub.api.content.repository.TopicTagRepository;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Tag(INTEGRATION)
class PublicPostApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

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

	@Test
	void exposesPublicPostListDetailAndFiltersFromDatabase() throws Exception {
		Instant now = Instant.parse("2026-05-02T02:00:00Z");
		Company company = companyRepository.saveAndFlush(Company.create("toss-public", "토스", "Toss"));
		SourceBlog sourceBlog = sourceBlogRepository.saveAndFlush(approvedSource(company));
		JobCategory backend = jobCategoryRepository.saveAndFlush(JobCategory.create("PublicBackend", "백엔드", 1));
		TopicTag spring = topicTagRepository.saveAndFlush(TopicTag.create("public-spring", "Spring", "spring"));

		ArchivedPost publishedPost = ArchivedPost.collect(
			sourceBlog,
			"toss-public-spring",
			"Spring 운영 경험",
			"https://toss.tech/spring",
			"https://toss.tech/spring",
			"fingerprint-public-toss-spring",
			now.minus(1, ChronoUnit.HOURS),
			now
		);
		publishedPost.replaceJobCategories(List.of(backend));
		publishedPost.replaceTopicTags(List.of(spring));
		publishedPost.markClassified();
		publishedPost.publish();
		archivedPostRepository.saveAndFlush(publishedPost);

		ArchivedPost hiddenPost = ArchivedPost.collect(
			sourceBlog,
			"toss-public-hidden",
			"Spring 숨김 글",
			"https://toss.tech/hidden",
			"https://toss.tech/hidden",
			"fingerprint-public-toss-hidden",
			now,
			now
		);
		hiddenPost.markClassified();
		hiddenPost.hide("테스트 숨김");
		archivedPostRepository.saveAndFlush(hiddenPost);

		aiSummaryRepository.saveAndFlush(AiSummary.ready(
			publishedPost,
			1,
			"Spring 운영 경험 요약",
			"[\"운영\", \"장애 대응\"]",
			"gpt-4.1-mini",
			"summary-v1",
			now
		));

		mockMvc.perform(get("/api/v1/public/posts")
				.param("q", "Spring 운영")
				.param("company", "toss-public")
				.param("job", "PublicBackend")
				.param("tag", "public-spring"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].slug").value("toss-public-spring"))
			.andExpect(jsonPath("$.content[0].sourceName").value("토스 기술 블로그"))
			.andExpect(jsonPath("$.content[0].summaryPreview").value("Spring 운영 경험 요약"));

		mockMvc.perform(get("/api/v1/public/posts/toss-public-spring"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.slug").value("toss-public-spring"))
			.andExpect(jsonPath("$.summary.headline").value("Spring 운영 경험 요약"))
			.andExpect(jsonPath("$.summary.bullets[0]").value("운영"));

		mockMvc.perform(get("/api/v1/public/posts/toss-public-hidden"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PUBLIC_POST_NOT_FOUND"));

		mockMvc.perform(get("/api/v1/public/filters"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.companies[0].slug").value("toss-public"))
			.andExpect(jsonPath("$.companies[0].count").value(1))
			.andExpect(jsonPath("$.jobs[0].code").value("PublicBackend"))
			.andExpect(jsonPath("$.jobs[0].count").value(1))
			.andExpect(jsonPath("$.tags[0].slug").value("public-spring"))
			.andExpect(jsonPath("$.tags[0].count").value(1));
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

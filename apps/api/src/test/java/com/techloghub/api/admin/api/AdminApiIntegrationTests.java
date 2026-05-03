package com.techloghub.api.admin.api;

import static com.techloghub.api.testsupport.TestTags.INTEGRATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techloghub.api.admin.domain.AdminUser;
import com.techloghub.api.admin.repository.AdminAuditLogRepository;
import com.techloghub.api.admin.repository.AdminUserRepository;
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
class AdminApiIntegrationTests {

	private static final String ADMIN_EMAIL = "admin@techloghub.local";
	private static final String ADMIN_PASSWORD = "admin1234";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private BCryptPasswordEncoder adminPasswordEncoder;

	@Autowired
	private AdminAuditLogRepository adminAuditLogRepository;

	@Autowired
	private AdminUserRepository adminUserRepository;

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
	void logsInReadsAdminSourcesAndPostsThenLogsOut() throws Exception {
		AdminUser adminUser = adminUserRepository.saveAndFlush(AdminUser.create(
			ADMIN_EMAIL,
			adminPasswordEncoder.encode(ADMIN_PASSWORD)
		));
		Instant now = Instant.parse("2026-05-04T01:00:00Z");
		Company company = companyRepository.saveAndFlush(Company.create("kakao-admin", "카카오", "Kakao"));
		SourceBlog sourceBlog = sourceBlogRepository.saveAndFlush(SourceBlog.propose(
			company,
			"카카오 기술 블로그",
			"https://tech.kakao.com",
			"https://tech.kakao.com/rss.xml",
			SourceType.RSS
		));
		JobCategory backend = jobCategoryRepository.saveAndFlush(JobCategory.create("AdminBackend", "백엔드", 1));
		TopicTag spring = topicTagRepository.saveAndFlush(TopicTag.create("admin-spring", "Spring", "spring"));
		ArchivedPost post = ArchivedPost.collect(
			sourceBlog,
			"kakao-admin-spring",
			"Spring 관리자 API",
			"https://tech.kakao.com/admin-spring",
			"https://tech.kakao.com/admin-spring",
			"fingerprint-kakao-admin-spring",
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
			"관리자 API 요약",
			"[\"운영\", \"관리\"]",
			"gpt-4.1-mini",
			"summary-v1",
			now
		));

		String loginResponse = mockMvc.perform(post("/api/v1/admin/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "admin@techloghub.local",
					  "password": "admin1234"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.admin.email").value(ADMIN_EMAIL))
			.andReturn()
			.getResponse()
			.getContentAsString();
		String accessToken = readText(loginResponse, "accessToken");

		mockMvc.perform(get("/api/v1/admin/auth/me")
				.header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(adminUser.getId()))
			.andExpect(jsonPath("$.role").value("admin"));

		mockMvc.perform(get("/api/v1/admin/sources")
				.header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
				.param("status", "proposed")
				.param("q", "카카오"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].id").value(sourceBlog.getId()))
			.andExpect(jsonPath("$.content[0].companySlug").value("kakao-admin"))
			.andExpect(jsonPath("$.content[0].status").value("proposed"));

		mockMvc.perform(get("/api/v1/admin/posts")
				.header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
				.param("company", "kakao-admin")
				.param("visibilityState", "published")
				.param("processingState", "classified")
				.param("summaryState", "ready")
				.param("size", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].id").value(post.getId()))
			.andExpect(jsonPath("$.content[0].summaryState").value("ready"));

		mockMvc.perform(get("/api/v1/admin/posts/{postId}", post.getId())
				.header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(post.getId()))
			.andExpect(jsonPath("$.jobCategories[0]").value("AdminBackend"))
			.andExpect(jsonPath("$.topicTags[0]").value("admin-spring"));

		mockMvc.perform(post("/api/v1/admin/auth/logout")
				.header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/admin/auth/me")
				.header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("ADMIN_SESSION_INVALID"));

		assertThat(adminAuditLogRepository.findByAdminUser_IdOrderByCreatedAtDesc(
			adminUser.getId(),
			PageRequest.of(0, 10)
		).map(adminAuditLog -> adminAuditLog.getActionType()).toList())
			.contains("ADMIN_LOGIN_SUCCESS", "ADMIN_LOGOUT");
	}

	@Test
	void rejectsInvalidLoginAndMissingAdminAuthorization() throws Exception {
		AdminUser adminUser = adminUserRepository.saveAndFlush(AdminUser.create(
			ADMIN_EMAIL,
			adminPasswordEncoder.encode(ADMIN_PASSWORD)
		));

		for (int attempt = 0; attempt < 5; attempt++) {
			mockMvc.perform(post("/api/v1/admin/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						  "email": "admin@techloghub.local",
						  "password": "wrong-password"
						}
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("ADMIN_INVALID_CREDENTIALS"));
		}

		mockMvc.perform(post("/api/v1/admin/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "admin@techloghub.local",
					  "password": "admin1234"
					}
					"""))
			.andExpect(status().isTooManyRequests())
			.andExpect(jsonPath("$.code").value("ADMIN_LOGIN_LOCKED"));

		mockMvc.perform(get("/api/v1/admin/sources"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("ADMIN_SESSION_INVALID"));

		assertThat(adminUserRepository.findByEmail(ADMIN_EMAIL)).get()
			.extracting(AdminUser::getFailedLoginCount)
			.isEqualTo(5);
		assertThat(adminAuditLogRepository.findByAdminUser_IdOrderByCreatedAtDesc(
			adminUser.getId(),
			PageRequest.of(0, 10)
		).map(adminAuditLog -> adminAuditLog.getActionType()).toList())
			.contains("ADMIN_LOGIN_FAILURE", "ADMIN_LOGIN_LOCKED");
	}

	private String readText(String json, String fieldName) throws Exception {
		JsonNode node = objectMapper.readTree(json);
		return node.get(fieldName).asText();
	}

	private String bearer(String token) {
		return "Bearer " + token;
	}
}

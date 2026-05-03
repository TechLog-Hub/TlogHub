package com.techloghub.api.admin.api;

import static com.techloghub.api.testsupport.TestTags.INTEGRATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techloghub.api.admin.domain.AdminUser;
import com.techloghub.api.admin.repository.AdminAuditLogRepository;
import com.techloghub.api.admin.repository.AdminUserRepository;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Tag(INTEGRATION)
class AdminAuthenticationPersistenceIntegrationTests {

	private static final String ADMIN_EMAIL = "admin-persistence@techloghub.local";
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

	@BeforeEach
	void setUp() {
		adminAuditLogRepository.deleteAllInBatch();
		adminUserRepository.deleteAllInBatch();
	}

	@Test
	void logoutInvalidatesPersistedSessionTokenAcrossRequests() throws Exception {
		AdminUser adminUser = adminUserRepository.saveAndFlush(AdminUser.create(
			ADMIN_EMAIL,
			adminPasswordEncoder.encode(ADMIN_PASSWORD)
		));

		String loginResponse = mockMvc.perform(post("/api/v1/admin/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "admin-persistence@techloghub.local",
					  "password": "admin1234"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andReturn()
			.getResponse()
			.getContentAsString();
		String accessToken = readText(loginResponse, "accessToken");

		mockMvc.perform(post("/api/v1/admin/auth/logout")
				.header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/admin/auth/me")
				.header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("ADMIN_SESSION_INVALID"));

		AdminUser persistedAdminUser = adminUserRepository.findById(adminUser.getId()).orElseThrow();
		assertThat(persistedAdminUser.getSessionTokenHash()).isNull();
		assertThat(persistedAdminUser.getSessionIssuedAt()).isNull();
		assertThat(persistedAdminUser.getSessionExpiresAt()).isNull();
	}

	private String readText(String json, String fieldName) throws Exception {
		JsonNode node = objectMapper.readTree(json);
		return node.get(fieldName).asText();
	}

	private String bearer(String token) {
		return "Bearer " + token;
	}
}

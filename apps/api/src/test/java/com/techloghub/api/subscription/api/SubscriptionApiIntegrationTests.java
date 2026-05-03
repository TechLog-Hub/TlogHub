package com.techloghub.api.subscription.api;

import static com.techloghub.api.testsupport.TestTags.INTEGRATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techloghub.api.content.domain.Company;
import com.techloghub.api.content.repository.CompanyRepository;

@Transactional
@ActiveProfiles("test")
@SpringBootTest(properties = "techlog.subscription.expose-dev-tokens=true")
@AutoConfigureMockMvc
@Tag(INTEGRATION)
class SubscriptionApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CompanyRepository companyRepository;

	@Test
	void requestsVerifiesManagesAndUnsubscribesCompanySubscriptions() throws Exception {
		companyRepository.saveAndFlush(Company.create("naver-subscription", "네이버", "Naver"));
		companyRepository.saveAndFlush(Company.create("kakao-subscription", "카카오", "Kakao"));
		companyRepository.saveAndFlush(Company.create("toss-subscription", "토스", "Toss"));

		String requestBody = """
			{
			  "email": "Dev@Example.com",
			  "companySlugs": ["naver-subscription", "kakao-subscription"]
			}
			""";

		String requestResponse = mockMvc.perform(post("/api/v1/subscriptions/requests")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.requestAccepted").value(true))
			.andExpect(jsonPath("$.maskedEmail").value("de***@example.com"))
			.andExpect(jsonPath("$.verificationToken").isNotEmpty())
			.andReturn()
			.getResponse()
			.getContentAsString();
		String verificationToken = readText(requestResponse, "verificationToken");

		String verifyResponse = mockMvc.perform(post("/api/v1/subscriptions/verify")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"" + verificationToken + "\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("active"))
			.andExpect(jsonPath("$.manageToken").isNotEmpty())
			.andReturn()
			.getResponse()
			.getContentAsString();
		String manageToken = readText(verifyResponse, "manageToken");

		mockMvc.perform(get("/api/v1/subscriptions/manage")
				.header(HttpHeaders.AUTHORIZATION, bearer(manageToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.maskedEmail").value("de***@example.com"))
			.andExpect(jsonPath("$.status").value("active"))
			.andExpect(jsonPath("$.companies.length()").value(2));

		mockMvc.perform(post("/api/v1/subscriptions/manage/companies")
				.header(HttpHeaders.AUTHORIZATION, bearer(manageToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"companySlugs\":[\"toss-subscription\"]}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.companies.length()").value(3));

		mockMvc.perform(delete("/api/v1/subscriptions/manage/companies/{companySlug}", "kakao-subscription")
				.header(HttpHeaders.AUTHORIZATION, bearer(manageToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.companies.length()").value(2));

		mockMvc.perform(post("/api/v1/subscriptions/manage/unsubscribe")
				.header(HttpHeaders.AUTHORIZATION, bearer(manageToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("unsubscribed"))
			.andExpect(jsonPath("$.companies.length()").value(0));

		mockMvc.perform(get("/api/v1/subscriptions/manage")
				.header(HttpHeaders.AUTHORIZATION, bearer(manageToken)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("SUBSCRIPTION_MANAGE_TOKEN_NOT_FOUND"));
	}

	@Test
	void rejectsUnknownCompanyAndInvalidVerificationToken() throws Exception {
		mockMvc.perform(post("/api/v1/subscriptions/requests")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "dev@example.com",
					  "companySlugs": ["unknown-company"]
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("SUBSCRIPTION_COMPANY_NOT_FOUND"));

		mockMvc.perform(post("/api/v1/subscriptions/verify")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"invalid-token\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("SUBSCRIPTION_VERIFICATION_TOKEN_INVALID"));
	}

	private String readText(String json, String fieldName) throws Exception {
		JsonNode node = objectMapper.readTree(json);
		return node.get(fieldName).asText();
	}

	private String bearer(String token) {
		return "Bearer " + token;
	}
}

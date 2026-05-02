package com.techloghub.api.publicapi.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.techloghub.api.common.dto.PageResponse;
import com.techloghub.api.common.error.BusinessException;
import com.techloghub.api.publicapi.application.PublicPostQuery;
import com.techloghub.api.publicapi.application.PublicPostQueryService;
import com.techloghub.api.publicapi.application.PublicPostSort;
import com.techloghub.api.publicapi.dto.PublicCompanyFilterResponse;
import com.techloghub.api.publicapi.dto.PublicCompanySummaryResponse;
import com.techloghub.api.publicapi.dto.PublicFilterMetadataResponse;
import com.techloghub.api.publicapi.dto.PublicJobCategoryFilterResponse;
import com.techloghub.api.publicapi.dto.PublicPostDetailResponse;
import com.techloghub.api.publicapi.dto.PublicPostListItemResponse;
import com.techloghub.api.publicapi.dto.PublicSourceSummaryResponse;
import com.techloghub.api.publicapi.dto.PublicTopicTagFilterResponse;
import com.techloghub.api.publicapi.error.PublicApiErrorCode;

@WebMvcTest(PublicPostController.class)
class PublicPostControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PublicPostQueryService publicPostQueryService;

	@Test
	void getsPostsWithDefaultAndRepeatedQueryParameters() throws Exception {
		given(publicPostQueryService.getPosts(any())).willReturn(new PageResponse<>(
			List.of(new PublicPostListItemResponse(
				1L,
				"toss-spring",
				"Spring 운영 경험",
				new PublicCompanySummaryResponse("toss", "토스"),
				Instant.parse("2026-05-02T01:00:00Z"),
				List.of("Backend"),
				List.of("spring"),
				"ready",
				"Spring 운영 경험 요약",
				"https://toss.tech/spring"
			)),
			0,
			24,
			1,
			1
		));

		mockMvc.perform(get("/api/v1/public/posts")
				.param("q", "Spring")
				.param("company", "toss")
				.param("company", "naver")
				.param("job", "Backend")
				.param("tag", "spring"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].slug").value("toss-spring"))
			.andExpect(jsonPath("$.content[0].company.slug").value("toss"))
			.andExpect(jsonPath("$.page").value(0))
			.andExpect(jsonPath("$.size").value(24))
			.andExpect(jsonPath("$.totalElements").value(1));

		ArgumentCaptor<PublicPostQuery> captor = ArgumentCaptor.forClass(PublicPostQuery.class);
		verify(publicPostQueryService).getPosts(captor.capture());
		assertThat(captor.getValue().companySlugs()).containsExactly("toss", "naver");
		assertThat(captor.getValue().sort()).isEqualTo(PublicPostSort.LATEST);
	}

	@Test
	void returnsBadRequestWhenSizeExceedsMax() throws Exception {
		mockMvc.perform(get("/api/v1/public/posts").param("size", "101"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
			.andExpect(jsonPath("$.message").value("size must be between 1 and 100"));
	}

	@Test
	void getsPostDetail() throws Exception {
		given(publicPostQueryService.getPost("toss-spring")).willReturn(new PublicPostDetailResponse(
			1L,
			"toss-spring",
			"Spring 운영 경험",
			new PublicCompanySummaryResponse("toss", "토스"),
			new PublicSourceSummaryResponse("토스 기술 블로그", "https://toss.tech"),
			Instant.parse("2026-05-02T01:00:00Z"),
			List.of("Backend"),
			List.of("spring"),
			"ready",
			null,
			"https://toss.tech/spring",
			"AI가 원문을 바탕으로 생성한 요약입니다."
		));

		mockMvc.perform(get("/api/v1/public/posts/toss-spring"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.slug").value("toss-spring"))
			.andExpect(jsonPath("$.source.name").value("토스 기술 블로그"));
	}

	@Test
	void returnsNotFoundWhenPublicPostDoesNotExist() throws Exception {
		given(publicPostQueryService.getPost("missing"))
			.willThrow(new BusinessException(PublicApiErrorCode.PUBLIC_POST_NOT_FOUND));

		mockMvc.perform(get("/api/v1/public/posts/missing"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PUBLIC_POST_NOT_FOUND"));
	}

	@Test
	void getsFilters() throws Exception {
		given(publicPostQueryService.getFilters()).willReturn(new PublicFilterMetadataResponse(
			List.of(new PublicCompanyFilterResponse("toss", "토스", 1)),
			List.of(new PublicJobCategoryFilterResponse("Backend", "백엔드", 1)),
			List.of(new PublicTopicTagFilterResponse("spring", "Spring", 1))
		));

		mockMvc.perform(get("/api/v1/public/filters"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.companies[0].slug").value("toss"))
			.andExpect(jsonPath("$.jobs[0].code").value("Backend"))
			.andExpect(jsonPath("$.tags[0].slug").value("spring"));
	}
}

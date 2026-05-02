package com.techloghub.api.publicapi.application;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techloghub.api.content.domain.AiSummary;
import com.techloghub.api.content.domain.ArchivedPost;
import com.techloghub.api.content.domain.Company;
import com.techloghub.api.content.domain.JobCategory;
import com.techloghub.api.content.domain.SummaryState;
import com.techloghub.api.content.domain.TopicTag;
import com.techloghub.api.content.repository.ArchivedPostListQueryDto;
import com.techloghub.api.publicapi.dto.PublicCompanyFilterResponse;
import com.techloghub.api.publicapi.dto.PublicFilterMetadataResponse;
import com.techloghub.api.publicapi.dto.PublicJobCategoryFilterResponse;
import com.techloghub.api.publicapi.dto.PublicPostDetailResponse;
import com.techloghub.api.publicapi.dto.PublicPostListItemResponse;
import com.techloghub.api.publicapi.dto.PublicPostSummaryResponse;
import com.techloghub.api.publicapi.dto.PublicTopicTagFilterResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 공개 글 조회 결과를 API 응답 DTO로 조립한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PublicPostResponseAssembler {

	private static final String SUMMARY_PENDING_PREVIEW = "AI 요약을 준비 중입니다.";
	private static final String SUMMARY_FAILED_PREVIEW = "AI 요약을 다시 준비 중입니다.";
	private static final String AI_NOTICE = "AI가 원문을 바탕으로 생성한 요약입니다.";
	private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
	};

	private final ObjectMapper objectMapper;

	public PublicPostListItemResponse assembleListItem(
		ArchivedPostListQueryDto post,
		List<String> jobCategories,
		List<String> topicTags,
		Optional<AiSummary> summary
	) {
		return PublicPostListItemResponse.of(
			post,
			jobCategories,
			topicTags,
			summaryState(summary),
			summaryPreview(summary)
		);
	}

	public PublicPostDetailResponse assembleDetail(
		ArchivedPost post,
		List<String> jobCategories,
		List<String> topicTags,
		Optional<AiSummary> summary
	) {
		return PublicPostDetailResponse.of(
			post,
			jobCategories,
			topicTags,
			summaryState(summary),
			summary.flatMap(this::summaryResponse).orElse(null),
			AI_NOTICE
		);
	}

	public PublicFilterMetadataResponse assembleFilterMetadata(
		List<Company> companies,
		Map<String, Long> companyCountMap,
		List<JobCategory> jobCategories,
		Map<String, Long> jobCountMap,
		List<TopicTag> topicTags,
		Map<String, Long> tagCountMap
	) {
		return PublicFilterMetadataResponse.of(
			companies.stream()
				.map(company -> PublicCompanyFilterResponse.of(
					company,
					companyCountMap.getOrDefault(company.getSlug(), 0L)
				))
				.toList(),
			jobCategories.stream()
				.map(jobCategory -> PublicJobCategoryFilterResponse.of(
					jobCategory,
					jobCountMap.getOrDefault(jobCategory.getCode(), 0L)
				))
				.toList(),
			topicTags.stream()
				.map(tag -> PublicTopicTagFilterResponse.of(
					tag,
					tagCountMap.getOrDefault(tag.getSlug(), 0L)
				))
				.toList()
		);
	}

	private String summaryState(Optional<AiSummary> summary) {
		return summary.map(AiSummary::getSummaryState)
			.orElse(SummaryState.PENDING)
			.name()
			.toLowerCase(Locale.ROOT);
	}

	private String summaryPreview(Optional<AiSummary> summary) {
		return summary
			.map(value -> switch (value.getSummaryState()) {
				case READY -> value.getHeadline();
				case FAILED -> SUMMARY_FAILED_PREVIEW;
				case PENDING, HIDDEN -> SUMMARY_PENDING_PREVIEW;
			})
			.orElse(SUMMARY_PENDING_PREVIEW);
	}

	private Optional<PublicPostSummaryResponse> summaryResponse(AiSummary summary) {
		if (summary.getSummaryState() != SummaryState.READY) {
			return Optional.empty();
		}
		return Optional.of(PublicPostSummaryResponse.of(
			summary.getHeadline(),
			parseBullets(summary)
		));
	}

	private List<String> parseBullets(AiSummary summary) {
		try {
			return objectMapper.readValue(summary.getBulletsJson(), STRING_LIST_TYPE);
		} catch (JsonProcessingException exception) {
			log.warn("AI 요약 bullet JSON 파싱에 실패했습니다. summaryId={}", summary.getId(), exception);
			return List.of();
		}
	}
}

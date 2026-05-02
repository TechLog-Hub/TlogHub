package com.techloghub.api.publicapi.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techloghub.api.common.dto.PageResponse;
import com.techloghub.api.common.error.BusinessException;
import com.techloghub.api.content.domain.AiSummary;
import com.techloghub.api.content.domain.ArchivedPost;
import com.techloghub.api.content.domain.Company;
import com.techloghub.api.content.domain.CompanyStatus;
import com.techloghub.api.content.domain.SummaryState;
import com.techloghub.api.content.domain.VisibilityState;
import com.techloghub.api.content.repository.AiSummaryRepository;
import com.techloghub.api.content.repository.ArchivedPostJobCategoryQueryDto;
import com.techloghub.api.content.repository.ArchivedPostListQueryDto;
import com.techloghub.api.content.repository.ArchivedPostRepository;
import com.techloghub.api.content.repository.ArchivedPostSearchCondition;
import com.techloghub.api.content.repository.ArchivedPostTopicTagQueryDto;
import com.techloghub.api.content.repository.CompanyRepository;
import com.techloghub.api.content.repository.FilterCountQueryDto;
import com.techloghub.api.content.repository.JobCategoryRepository;
import com.techloghub.api.content.repository.TopicTagRepository;
import com.techloghub.api.publicapi.dto.PublicCompanyFilterResponse;
import com.techloghub.api.publicapi.dto.PublicCompanySummaryResponse;
import com.techloghub.api.publicapi.dto.PublicFilterMetadataResponse;
import com.techloghub.api.publicapi.dto.PublicJobCategoryFilterResponse;
import com.techloghub.api.publicapi.dto.PublicPostDetailResponse;
import com.techloghub.api.publicapi.dto.PublicPostListItemResponse;
import com.techloghub.api.publicapi.dto.PublicPostSummaryResponse;
import com.techloghub.api.publicapi.dto.PublicSourceSummaryResponse;
import com.techloghub.api.publicapi.dto.PublicTopicTagFilterResponse;
import com.techloghub.api.publicapi.error.PublicApiErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 공개 사용자의 글 목록, 상세, 필터 메타데이터 조회를 담당한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicPostQueryService {

	private static final String SUMMARY_PENDING_PREVIEW = "AI 요약을 준비 중입니다.";
	private static final String SUMMARY_FAILED_PREVIEW = "AI 요약을 다시 준비 중입니다.";
	private static final String AI_NOTICE = "AI가 원문을 바탕으로 생성한 요약입니다.";
	private static final String FILTER_METADATA_CACHE = "public-filter-metadata";
	private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
	};

	private final ArchivedPostRepository archivedPostRepository;
	private final AiSummaryRepository aiSummaryRepository;
	private final CompanyRepository companyRepository;
	private final JobCategoryRepository jobCategoryRepository;
	private final TopicTagRepository topicTagRepository;
	private final ObjectMapper objectMapper;

	public PageResponse<PublicPostListItemResponse> getPosts(PublicPostQuery query) {
		Page<ArchivedPostListQueryDto> postPage = archivedPostRepository.searchPublished(
			ArchivedPostSearchCondition.of(
				query.keyword(),
				query.companySlugs(),
				query.jobCodes(),
				query.tagSlugs()
			),
			PageRequest.of(query.page(), query.size(), sort(query.sort()))
		);
		List<Long> postIds = postPage.getContent().stream()
			.map(ArchivedPostListQueryDto::id)
			.toList();
		Map<Long, List<String>> jobCategoryMap = jobCategoryMap(postIds);
		Map<Long, List<String>> topicTagMap = topicTagMap(postIds);
		Map<Long, AiSummary> summaryMap = summaryMap(postIds);

		List<PublicPostListItemResponse> responses = postPage.getContent().stream()
			.map(post -> toListItemResponse(
				post,
				jobCategoryMap.getOrDefault(post.id(), List.of()),
				topicTagMap.getOrDefault(post.id(), List.of()),
				Optional.ofNullable(summaryMap.get(post.id()))
			))
			.toList();

		return PageResponse.of(responses, postPage);
	}

	public PublicPostDetailResponse getPost(String slug) {
		ArchivedPost post = archivedPostRepository
			.findDetailedBySlugAndVisibilityState(slug, VisibilityState.PUBLISHED)
			.orElseThrow(() -> new BusinessException(PublicApiErrorCode.PUBLIC_POST_NOT_FOUND));
		Optional<AiSummary> summary = aiSummaryRepository.findByArchivedPost_IdAndCurrentTrue(post.getId());
		List<String> jobCategoryCodes = jobCategoryMap(List.of(post.getId()))
			.getOrDefault(post.getId(), List.of())
			.stream()
			.sorted()
			.toList();
		List<String> topicTagSlugs = topicTagMap(List.of(post.getId()))
			.getOrDefault(post.getId(), List.of())
			.stream()
			.sorted()
			.toList();

		return new PublicPostDetailResponse(
			post.getId(),
			post.getSlug(),
			post.getTitle(),
			new PublicCompanySummaryResponse(post.getCompany().getSlug(), post.getCompany().getNameKo()),
			new PublicSourceSummaryResponse(post.getSourceBlog().getName(), post.getSourceBlog().getHomepageUrl()),
			post.getPublishedAt(),
			jobCategoryCodes,
			topicTagSlugs,
			summaryState(summary),
			summary.flatMap(this::summaryResponse).orElse(null),
			post.getOriginUrl(),
			AI_NOTICE
		);
	}

	@Cacheable(cacheNames = FILTER_METADATA_CACHE)
	public PublicFilterMetadataResponse getFilters() {
		Map<String, Long> companyCountMap = countMap(archivedPostRepository.countPublishedPostsByCompany());
		Map<String, Long> jobCountMap = countMap(archivedPostRepository.countPublishedPostsByJobCategory());
		Map<String, Long> tagCountMap = countMap(archivedPostRepository.countPublishedPostsByTopicTag());

		return new PublicFilterMetadataResponse(
			companyRepository.findByStatusOrderByNameKoAsc(CompanyStatus.ACTIVE).stream()
				.map(company -> new PublicCompanyFilterResponse(
					company.getSlug(),
					company.getNameKo(),
					companyCountMap.getOrDefault(company.getSlug(), 0L)
				))
				.toList(),
			jobCategoryRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
				.map(jobCategory -> new PublicJobCategoryFilterResponse(
					jobCategory.getCode(),
					jobCategory.getLabelKo(),
					jobCountMap.getOrDefault(jobCategory.getCode(), 0L)
				))
				.toList(),
			topicTagRepository.findByActiveTrueOrderByLabelAsc().stream()
				.map(tag -> new PublicTopicTagFilterResponse(
					tag.getSlug(),
					tag.getLabel(),
					tagCountMap.getOrDefault(tag.getSlug(), 0L)
				))
				.toList()
		);
	}

	private PublicPostListItemResponse toListItemResponse(
		ArchivedPostListQueryDto post,
		List<String> jobCategories,
		List<String> topicTags,
		Optional<AiSummary> summary
	) {
		return new PublicPostListItemResponse(
			post.id(),
			post.slug(),
			post.title(),
			new PublicCompanySummaryResponse(post.companySlug(), post.companyNameKo()),
			post.publishedAt(),
			jobCategories,
			topicTags,
			summaryState(summary),
			summaryPreview(summary),
			post.originUrl()
		);
	}

	private Sort sort(PublicPostSort sort) {
		return switch (sort) {
			case LATEST, RELEVANCE -> Sort.by(
				Sort.Order.desc("publishedAt"),
				Sort.Order.desc("id")
			);
		};
	}

	private Map<Long, List<String>> jobCategoryMap(List<Long> postIds) {
		return archivedPostRepository.findJobCategoriesByPostIds(postIds).stream()
			.collect(Collectors.groupingBy(
				ArchivedPostJobCategoryQueryDto::postId,
				LinkedHashMap::new,
				Collectors.mapping(ArchivedPostJobCategoryQueryDto::code, Collectors.toList())
			));
	}

	private Map<Long, List<String>> topicTagMap(List<Long> postIds) {
		return archivedPostRepository.findTopicTagsByPostIds(postIds).stream()
			.collect(Collectors.groupingBy(
				ArchivedPostTopicTagQueryDto::postId,
				LinkedHashMap::new,
				Collectors.mapping(ArchivedPostTopicTagQueryDto::slug, Collectors.toList())
			));
	}

	private Map<Long, AiSummary> summaryMap(List<Long> postIds) {
		if (postIds.isEmpty()) {
			return Map.of();
		}
		return aiSummaryRepository.findByArchivedPost_IdInAndCurrentTrue(postIds).stream()
			.collect(Collectors.toMap(
				summary -> summary.getArchivedPost().getId(),
				Function.identity(),
				(first, second) -> first
			));
	}

	private Map<String, Long> countMap(List<FilterCountQueryDto> rows) {
		return rows.stream()
			.collect(Collectors.toMap(FilterCountQueryDto::key, FilterCountQueryDto::count));
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
		return Optional.of(new PublicPostSummaryResponse(
			summary.getHeadline(),
			parseBullets(summary)
		));
	}

	private List<String> parseBullets(AiSummary summary) {
		try {
			return objectMapper.readValue(summary.getBulletsJson(), STRING_LIST_TYPE);
		} catch (JsonProcessingException exception) {
			log.warn("Failed to parse AI summary bullets. summaryId={}", summary.getId(), exception);
			return List.of();
		}
	}
}

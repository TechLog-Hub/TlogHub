package com.techloghub.api.publicapi.application;

import java.util.LinkedHashMap;
import java.util.List;
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

import com.techloghub.api.common.dto.PageResponse;
import com.techloghub.api.common.error.BusinessException;
import com.techloghub.api.common.util.CollectionSupport;
import com.techloghub.api.content.domain.AiSummary;
import com.techloghub.api.content.domain.ArchivedPost;
import com.techloghub.api.content.domain.Company;
import com.techloghub.api.content.domain.CompanyStatus;
import com.techloghub.api.content.domain.JobCategory;
import com.techloghub.api.content.domain.TopicTag;
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
import com.techloghub.api.publicapi.dto.PublicFilterMetadataResponse;
import com.techloghub.api.publicapi.dto.PublicPostDetailResponse;
import com.techloghub.api.publicapi.dto.PublicPostListItemResponse;
import com.techloghub.api.publicapi.error.PublicApiErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 공개 사용자의 글 목록, 상세, 필터 메타데이터 조회를 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicPostQueryService {

	private static final String FILTER_METADATA_CACHE = "public-filter-metadata";

	private final ArchivedPostRepository archivedPostRepository;
	private final AiSummaryRepository aiSummaryRepository;
	private final CompanyRepository companyRepository;
	private final JobCategoryRepository jobCategoryRepository;
	private final TopicTagRepository topicTagRepository;
	private final PublicPostResponseAssembler publicPostResponseAssembler;

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
			.map(post -> publicPostResponseAssembler.assembleListItem(
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

		return publicPostResponseAssembler.assembleDetail(
			post,
			jobCategoryCodes,
			topicTagSlugs,
			summary
		);
	}

	@Cacheable(cacheNames = FILTER_METADATA_CACHE)
	public PublicFilterMetadataResponse getFilters() {
		Map<String, Long> companyCountMap = countMap(archivedPostRepository.countPublishedPostsByCompany());
		Map<String, Long> jobCountMap = countMap(archivedPostRepository.countPublishedPostsByJobCategory());
		Map<String, Long> tagCountMap = countMap(archivedPostRepository.countPublishedPostsByTopicTag());
		List<Company> companies = companyRepository.findByStatusOrderByNameKoAsc(CompanyStatus.ACTIVE);
		List<JobCategory> jobCategories = jobCategoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
		List<TopicTag> topicTags = topicTagRepository.findByActiveTrueOrderByLabelAsc();

		return publicPostResponseAssembler.assembleFilterMetadata(
			companies,
			companyCountMap,
			jobCategories,
			jobCountMap,
			topicTags,
			tagCountMap
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
		return CollectionSupport.toValueMapStrict(rows, FilterCountQueryDto::key, FilterCountQueryDto::count);
	}
}

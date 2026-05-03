package com.techloghub.api.admin.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techloghub.api.admin.dto.AdminPostDetailResponse;
import com.techloghub.api.admin.dto.AdminPostListItemResponse;
import com.techloghub.api.admin.dto.AdminPostSearchRequest;
import com.techloghub.api.admin.error.AdminErrorCode;
import com.techloghub.api.common.dto.PageResponse;
import com.techloghub.api.common.error.BusinessException;
import com.techloghub.api.content.domain.ArchivedPost;
import com.techloghub.api.content.repository.AdminArchivedPostQueryDto;
import com.techloghub.api.content.repository.AdminArchivedPostSearchCondition;
import com.techloghub.api.content.repository.ArchivedPostRepository;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 글 목록과 상세 조회를 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPostQueryService {

	private final ArchivedPostRepository archivedPostRepository;

	public PageResponse<AdminPostListItemResponse> getPosts(AdminPostSearchRequest request) {
		Page<AdminArchivedPostQueryDto> page = archivedPostRepository.searchAdminPosts(
			AdminArchivedPostSearchCondition.of(
				request.keyword(),
				request.companySlug(),
				request.parsedProcessingState(),
				request.parsedVisibilityState(),
				request.parsedSummaryState()
			),
			PageRequest.of(
				request.pageValue(),
				request.sizeValue(),
				Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id"))
			)
		);
		return PageResponse.of(
			page.getContent().stream()
				.map(AdminPostListItemResponse::from)
				.toList(),
			page
		);
	}

	public AdminPostDetailResponse getPost(Long postId) {
		ArchivedPost post = archivedPostRepository.findById(postId)
			.orElseThrow(() -> new BusinessException(AdminErrorCode.ADMIN_POST_NOT_FOUND));
		return AdminPostDetailResponse.from(post);
	}
}

package com.techloghub.api.admin.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techloghub.api.common.dto.PageResponse;
import com.techloghub.api.content.repository.AdminSourceBlogQueryDto;
import com.techloghub.api.content.repository.AdminSourceBlogSearchCondition;
import com.techloghub.api.content.repository.SourceBlogRepository;
import com.techloghub.api.admin.dto.AdminSourceListItemResponse;
import com.techloghub.api.admin.dto.AdminSourceSearchRequest;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 소스 목록 조회를 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSourceQueryService {

	private final SourceBlogRepository sourceBlogRepository;

	public PageResponse<AdminSourceListItemResponse> getSources(AdminSourceSearchRequest request) {
		Page<AdminSourceBlogQueryDto> page = sourceBlogRepository.searchAdminSources(
			AdminSourceBlogSearchCondition.of(request.keyword(), request.parsedStatus()),
			PageRequest.of(
				request.pageValue(),
				request.sizeValue(),
				Sort.by(Sort.Order.desc("id"))
			)
		);
		return PageResponse.of(
			page.getContent().stream()
				.map(AdminSourceListItemResponse::from)
				.toList(),
			page
		);
	}
}

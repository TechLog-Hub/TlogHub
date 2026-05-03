package com.techloghub.api.admin.api;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techloghub.api.admin.application.AdminPostQueryService;
import com.techloghub.api.admin.dto.AdminPostDetailResponse;
import com.techloghub.api.admin.dto.AdminPostListItemResponse;
import com.techloghub.api.admin.dto.AdminPostSearchRequest;
import com.techloghub.api.common.dto.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 글 운영 API다.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/posts")
public class AdminPostController {

	private final AdminPostQueryService adminPostQueryService;

	@GetMapping
	public PageResponse<AdminPostListItemResponse> getPosts(
		@Valid AdminPostSearchRequest request
	) {
		return adminPostQueryService.getPosts(request);
	}

	@GetMapping("/{postId}")
	public AdminPostDetailResponse getPost(
		@PathVariable @Positive Long postId
	) {
		return adminPostQueryService.getPost(postId);
	}
}

package com.techloghub.api.publicapi.api;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techloghub.api.common.dto.PageResponse;
import com.techloghub.api.publicapi.application.PublicPostQueryService;
import com.techloghub.api.publicapi.dto.PublicFilterMetadataResponse;
import com.techloghub.api.publicapi.dto.PublicPostDetailResponse;
import com.techloghub.api.publicapi.dto.PublicPostListItemResponse;
import com.techloghub.api.publicapi.dto.PublicPostSearchRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

/**
 * 공개 사용자의 기술 블로그 글 조회 API다.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public")
public class PublicPostController {

	private final PublicPostQueryService publicPostQueryService;

	@GetMapping("/posts")
	public PageResponse<PublicPostListItemResponse> getPosts(@Valid PublicPostSearchRequest request) {
		return publicPostQueryService.getPosts(request.toQuery());
	}

	@GetMapping("/posts/{slug}")
	public PublicPostDetailResponse getPost(@PathVariable @NotBlank String slug) {
		return publicPostQueryService.getPost(slug);
	}

	@GetMapping("/filters")
	public PublicFilterMetadataResponse getFilters() {
		return publicPostQueryService.getFilters();
	}
}

package com.techloghub.api.admin.api;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techloghub.api.admin.application.AdminSourceQueryService;
import com.techloghub.api.admin.dto.AdminSourceListItemResponse;
import com.techloghub.api.admin.dto.AdminSourceSearchRequest;
import com.techloghub.api.common.dto.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 소스 운영 API다.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/sources")
public class AdminSourceController {

	private final AdminSourceQueryService adminSourceQueryService;

	@GetMapping
	public PageResponse<AdminSourceListItemResponse> getSources(
		@Valid AdminSourceSearchRequest request
	) {
		return adminSourceQueryService.getSources(request);
	}
}

package com.techloghub.api.admin.api;

import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techloghub.api.admin.application.AdminAuthenticationService;
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

	private final AdminAuthenticationService adminAuthenticationService;
	private final AdminSourceQueryService adminSourceQueryService;

	@GetMapping
	public PageResponse<AdminSourceListItemResponse> getSources(
		@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
		@Valid AdminSourceSearchRequest request
	) {
		adminAuthenticationService.requireAdmin(authorization);
		return adminSourceQueryService.getSources(request);
	}
}

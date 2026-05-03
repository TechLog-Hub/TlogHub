package com.techloghub.api.admin.api;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techloghub.api.admin.application.AdminAuthenticationService;
import com.techloghub.api.admin.domain.AdminUser;
import com.techloghub.api.admin.dto.AdminLoginRequest;
import com.techloghub.api.admin.dto.AdminLoginResponse;
import com.techloghub.api.admin.dto.AdminMeResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 인증 API다.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

	private final AdminAuthenticationService adminAuthenticationService;

	@PostMapping("/login")
	public AdminLoginResponse login(@Valid @RequestBody AdminLoginRequest request) {
		return adminAuthenticationService.login(request.email(), request.password());
	}

	@PostMapping("/logout")
	public void logout(
		@RequestAttribute(AdminAuthenticationService.ADMIN_USER_REQUEST_ATTRIBUTE) AdminUser adminUser
	) {
		adminAuthenticationService.logout(adminUser);
	}

	@GetMapping("/me")
	public AdminMeResponse me(
		@RequestAttribute(AdminAuthenticationService.ADMIN_USER_REQUEST_ATTRIBUTE) AdminUser adminUser
	) {
		return AdminMeResponse.from(adminUser);
	}
}

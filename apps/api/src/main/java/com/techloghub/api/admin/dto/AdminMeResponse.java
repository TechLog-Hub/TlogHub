package com.techloghub.api.admin.dto;

import java.util.Locale;

import com.techloghub.api.admin.domain.AdminUser;

/**
 * 현재 관리자 정보 응답이다.
 */
public record AdminMeResponse(
	Long id,
	String email,
	String role
) {
	public static AdminMeResponse from(AdminUser adminUser) {
		return new AdminMeResponse(
			adminUser.getId(),
			adminUser.getEmail(),
			adminUser.getRole().name().toLowerCase(Locale.ROOT)
		);
	}
}

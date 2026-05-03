package com.techloghub.api.admin.dto;

import java.time.Instant;

import com.techloghub.api.admin.domain.AdminUser;

/**
 * 관리자 로그인 성공 응답이다.
 */
public record AdminLoginResponse(
	String accessToken,
	String tokenType,
	Instant expiresAt,
	AdminMeResponse admin
) {
	private static final String BEARER_TOKEN_TYPE = "Bearer";

	public static AdminLoginResponse of(AdminUser adminUser, String accessToken, Instant expiresAt) {
		return new AdminLoginResponse(
			accessToken,
			BEARER_TOKEN_TYPE,
			expiresAt,
			AdminMeResponse.from(adminUser)
		);
	}
}

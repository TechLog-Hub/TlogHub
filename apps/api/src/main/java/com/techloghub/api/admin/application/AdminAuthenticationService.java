package com.techloghub.api.admin.application;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techloghub.api.admin.domain.AdminAuditLog;
import com.techloghub.api.admin.domain.AdminUser;
import com.techloghub.api.admin.dto.AdminLoginResponse;
import com.techloghub.api.admin.dto.AdminMeResponse;
import com.techloghub.api.admin.error.AdminErrorCode;
import com.techloghub.api.admin.repository.AdminAuditLogRepository;
import com.techloghub.api.admin.repository.AdminUserRepository;
import com.techloghub.api.common.error.BusinessException;
import com.techloghub.api.common.util.CryptoSupport;
import com.techloghub.api.common.util.StringNormalizer;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 로그인, bearer token 검증, 로그아웃을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminAuthenticationService {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String AUDIT_TARGET_ADMIN_USER = "admin_user";
	private static final String ACTION_ADMIN_LOGOUT = "ADMIN_LOGOUT";

	private final AdminUserRepository adminUserRepository;
	private final AdminAuditLogRepository adminAuditLogRepository;
	private final BCryptPasswordEncoder adminPasswordEncoder;
	private final AdminSessionTokenGenerator adminSessionTokenGenerator;

	@Value("${techlog.admin.session-ttl:PT12H}")
	private Duration sessionTtl;

	public AdminLoginResponse login(String email, String password) {
		String normalizedEmail = normalizeEmail(email);
		AdminUser adminUser = adminUserRepository.findByEmail(normalizedEmail)
			.filter(AdminUser::isActive)
			.orElseThrow(() -> new BusinessException(AdminErrorCode.ADMIN_INVALID_CREDENTIALS));
		if (!adminPasswordEncoder.matches(password, adminUser.getPasswordHash())) {
			throw new BusinessException(AdminErrorCode.ADMIN_INVALID_CREDENTIALS);
		}

		String sessionToken = adminSessionTokenGenerator.generate();
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(sessionTtl);
		adminUser.issueSession(tokenHash(sessionToken), issuedAt, expiresAt);
		return AdminLoginResponse.of(adminUser, sessionToken, expiresAt);
	}

	@Transactional(readOnly = true)
	public AdminMeResponse me(String authorization) {
		return AdminMeResponse.from(requireAdmin(authorization));
	}

	public void logout(String authorization) {
		AdminUser adminUser = requireAdmin(authorization);
		adminUser.clearSession();
		adminAuditLogRepository.save(AdminAuditLog.record(
			adminUser,
			ACTION_ADMIN_LOGOUT,
			AUDIT_TARGET_ADMIN_USER,
			adminUser.getId(),
			null,
			null
		));
	}

	@Transactional(readOnly = true)
	public AdminUser requireAdmin(String authorization) {
		String sessionToken = bearerToken(authorization);
		String sessionTokenHash = tokenHash(sessionToken);
		Instant now = Instant.now();
		return adminUserRepository.findBySessionTokenHash(sessionTokenHash)
			.filter(adminUser -> adminUser.hasValidSession(now))
			.orElseThrow(() -> new BusinessException(AdminErrorCode.ADMIN_SESSION_INVALID));
	}

	private String bearerToken(String authorization) {
		String normalizedAuthorization = StringNormalizer.trimToNull(authorization);
		if (normalizedAuthorization == null || !normalizedAuthorization.startsWith(BEARER_PREFIX)) {
			throw new BusinessException(AdminErrorCode.ADMIN_SESSION_INVALID);
		}
		String token = StringNormalizer.trimToNull(normalizedAuthorization.substring(BEARER_PREFIX.length()));
		if (token == null) {
			throw new BusinessException(AdminErrorCode.ADMIN_SESSION_INVALID);
		}
		return token;
	}

	private String normalizeEmail(String email) {
		String normalizedEmail = StringNormalizer.trimToNull(email);
		if (normalizedEmail == null) {
			throw new BusinessException(AdminErrorCode.ADMIN_INVALID_CREDENTIALS);
		}
		return normalizedEmail.toLowerCase(Locale.ROOT);
	}

	private String tokenHash(String token) {
		return CryptoSupport.sha256Hex(token);
	}
}

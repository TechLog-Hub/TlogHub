package com.techloghub.api.admin.application;

import java.time.Duration;
import java.time.Instant;
import java.time.Clock;
import java.util.Locale;
import java.util.Optional;

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
	public static final String ADMIN_USER_REQUEST_ATTRIBUTE = "techlog.adminUser";
	private static final String AUDIT_TARGET_ADMIN_USER = "admin_user";
	private static final String AUDIT_TARGET_ADMIN_AUTH = "admin_auth";
	private static final long AUDIT_TARGET_EMPTY_ID = 0L;
	private static final String ACTION_ADMIN_LOGIN_SUCCESS = "ADMIN_LOGIN_SUCCESS";
	private static final String ACTION_ADMIN_LOGIN_FAILURE = "ADMIN_LOGIN_FAILURE";
	private static final String ACTION_ADMIN_LOGIN_LOCKED = "ADMIN_LOGIN_LOCKED";
	private static final String ACTION_ADMIN_LOGOUT = "ADMIN_LOGOUT";
	private static final String AUDIT_AFTER_SUCCESS = "{\"result\":\"success\"}";
	private static final String AUDIT_AFTER_INVALID_CREDENTIALS = "{\"result\":\"failure\",\"reason\":\"invalid_credentials\"}";
	private static final String AUDIT_AFTER_LOCKED = "{\"result\":\"failure\",\"reason\":\"locked\"}";

	private final AdminUserRepository adminUserRepository;
	private final AdminAuditLogRepository adminAuditLogRepository;
	private final BCryptPasswordEncoder adminPasswordEncoder;
	private final AdminSessionTokenGenerator adminSessionTokenGenerator;
	private final Clock clock;

	@Value("${techlog.admin.session-ttl:PT12H}")
	private Duration sessionTtl;

	@Value("${techlog.admin.max-failed-login-attempts:5}")
	private int maxFailedLoginAttempts;

	@Value("${techlog.admin.login-lock-duration:PT15M}")
	private Duration loginLockDuration;

	public AdminLoginResponse login(String email, String password) {
		String normalizedEmail = normalizeEmail(email);
		Optional<AdminUser> adminUserOptional = adminUserRepository.findByEmail(normalizedEmail)
			.filter(AdminUser::isActive);
		if (adminUserOptional.isEmpty()) {
			recordLoginFailure(null);
			throw new BusinessException(AdminErrorCode.ADMIN_INVALID_CREDENTIALS);
		}

		AdminUser adminUser = adminUserOptional.get();
		Instant now = clock.instant();
		if (adminUser.isLoginLocked(now)) {
			recordLoginLocked(adminUser);
			throw new BusinessException(AdminErrorCode.ADMIN_LOGIN_LOCKED);
		}

		if (!adminPasswordEncoder.matches(password, adminUser.getPasswordHash())) {
			adminUser.recordLoginFailure(now, maxFailedLoginAttempts, loginLockDuration);
			recordLoginFailure(adminUser);
			throw new BusinessException(AdminErrorCode.ADMIN_INVALID_CREDENTIALS);
		}

		String sessionToken = adminSessionTokenGenerator.generate();
		Instant issuedAt = clock.instant();
		Instant expiresAt = issuedAt.plus(sessionTtl);
		adminUser.clearLoginFailures();
		adminUser.issueSession(tokenHash(sessionToken), issuedAt, expiresAt);
		recordLoginSuccess(adminUser);
		return AdminLoginResponse.of(adminUser, sessionToken, expiresAt);
	}

	@Transactional(readOnly = true)
	public AdminMeResponse me(String authorization) {
		return AdminMeResponse.from(requireAdmin(authorization));
	}

	public void logout(String authorization) {
		AdminUser adminUser = requireAdmin(authorization);
		logout(adminUser);
	}

	public void logout(AdminUser adminUser) {
		AdminUser targetAdminUser = adminUserRepository.findById(adminUser.getId())
			.filter(AdminUser::isActive)
			.orElseThrow(() -> new BusinessException(AdminErrorCode.ADMIN_SESSION_INVALID));
		targetAdminUser.clearSession();
		adminAuditLogRepository.save(AdminAuditLog.record(
			targetAdminUser,
			ACTION_ADMIN_LOGOUT,
			AUDIT_TARGET_ADMIN_USER,
			targetAdminUser.getId(),
			null,
			null
		));
	}

	@Transactional(readOnly = true)
	public AdminUser requireAdmin(String authorization) {
		String sessionToken = bearerToken(authorization);
		String sessionTokenHash = tokenHash(sessionToken);
		Instant now = clock.instant();
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

	private void recordLoginSuccess(AdminUser adminUser) {
		adminAuditLogRepository.save(AdminAuditLog.record(
			adminUser,
			ACTION_ADMIN_LOGIN_SUCCESS,
			AUDIT_TARGET_ADMIN_USER,
			adminUser.getId(),
			null,
			AUDIT_AFTER_SUCCESS
		));
	}

	private void recordLoginFailure(AdminUser adminUser) {
		adminAuditLogRepository.save(AdminAuditLog.record(
			adminUser,
			ACTION_ADMIN_LOGIN_FAILURE,
			adminUser == null ? AUDIT_TARGET_ADMIN_AUTH : AUDIT_TARGET_ADMIN_USER,
			adminUser == null ? AUDIT_TARGET_EMPTY_ID : adminUser.getId(),
			null,
			AUDIT_AFTER_INVALID_CREDENTIALS
		));
	}

	private void recordLoginLocked(AdminUser adminUser) {
		adminAuditLogRepository.save(AdminAuditLog.record(
			adminUser,
			ACTION_ADMIN_LOGIN_LOCKED,
			AUDIT_TARGET_ADMIN_USER,
			adminUser.getId(),
			null,
			AUDIT_AFTER_LOCKED
		));
	}
}

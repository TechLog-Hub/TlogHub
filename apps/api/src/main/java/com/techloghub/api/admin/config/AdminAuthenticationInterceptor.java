package com.techloghub.api.admin.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.techloghub.api.admin.application.AdminAuthenticationService;
import com.techloghub.api.admin.domain.AdminUser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 API 공통 bearer 인증을 처리한다.
 */
@Component
@ConditionalOnBean(AdminAuthenticationService.class)
@RequiredArgsConstructor
public class AdminAuthenticationInterceptor implements HandlerInterceptor {

	private final AdminAuthenticationService adminAuthenticationService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		AdminUser adminUser = adminAuthenticationService.requireAdmin(request.getHeader(HttpHeaders.AUTHORIZATION));
		request.setAttribute(AdminAuthenticationService.ADMIN_USER_REQUEST_ATTRIBUTE, adminUser);
		return true;
	}
}

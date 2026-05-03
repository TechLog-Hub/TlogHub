package com.techloghub.api.admin.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 API Web MVC 설정이다.
 */
@Configuration
@ConditionalOnBean(AdminAuthenticationInterceptor.class)
@RequiredArgsConstructor
public class AdminWebMvcConfiguration implements WebMvcConfigurer {

	private static final String ADMIN_API_PATTERN = "/api/v1/admin/**";
	private static final String ADMIN_LOGIN_PATH = "/api/v1/admin/auth/login";

	private final AdminAuthenticationInterceptor adminAuthenticationInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(adminAuthenticationInterceptor)
			.addPathPatterns(ADMIN_API_PATTERN)
			.excludePathPatterns(ADMIN_LOGIN_PATH);
	}
}

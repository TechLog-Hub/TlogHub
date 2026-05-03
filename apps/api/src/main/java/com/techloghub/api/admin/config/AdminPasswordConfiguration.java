package com.techloghub.api.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 관리자 비밀번호 hash와 검증에 사용하는 password encoder 설정이다.
 */
@Configuration
public class AdminPasswordConfiguration {

	private static final int BCRYPT_STRENGTH = 12;

	@Bean
	public BCryptPasswordEncoder adminPasswordEncoder() {
		return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
	}
}

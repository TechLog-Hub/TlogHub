package com.techloghub.api.common.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 시간 의존 로직을 테스트에서 제어할 수 있도록 Clock bean을 제공한다.
 */
@Configuration
public class TimeConfiguration {

	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}
}

package com.techloghub.api.worker.batch;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * RSS Batch Worker 실행 설정을 타입 안전하게 바인딩한다.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "techlog.batch")
public class TechlogBatchProperties {

	private boolean scheduleEnabled = false;

	@NotBlank
	private String collectCron = "0 */30 * * * *";

	@Min(1)
	private int maxEntriesPerSource = 100;

	@Min(1024)
	private int maxFeedResponseBytes = 2_097_152;

	@NotNull
	private Duration feedConnectTimeout = Duration.ofSeconds(3);

	@NotNull
	private Duration feedReadTimeout = Duration.ofSeconds(10);

	@NotBlank
	private String userAgent = "TechlogHubBot/0.1";
}

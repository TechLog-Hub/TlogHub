package com.techloghub.api.worker.batch;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 설정으로 명시적으로 켠 환경에서만 RSS 수집 job을 주기 실행한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "techlog.batch", name = "schedule-enabled", havingValue = "true")
public class ScheduledBatchRunner {

	private final BatchJobLauncherService batchJobLauncherService;

	@Scheduled(cron = "${techlog.batch.collect-cron}")
	public void collectFeeds() {
		try {
			batchJobLauncherService.runRssFeedCollection("scheduler", "scheduled-rss-collection");
		} catch (BatchJobAlreadyRunningException exception) {
			log.warn("RSS feed collection job is already running. skip scheduled execution.");
		}
	}
}

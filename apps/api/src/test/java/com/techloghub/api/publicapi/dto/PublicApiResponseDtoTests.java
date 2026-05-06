package com.techloghub.api.publicapi.dto;

import static com.techloghub.api.testsupport.TestTags.UNIT;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(UNIT)
class PublicApiResponseDtoTests {

	@Test
	void replacesNullListFieldsWithEmptyLists() {
		PublicPostListItemResponse listItem = new PublicPostListItemResponse(
			1L,
			"toss-spring",
			"Spring 운영 경험",
			PublicCompanySummaryResponse.of("toss", "토스"),
			"토스 기술 블로그",
			Instant.parse("2026-05-02T01:00:00Z"),
			null,
			null,
			"ready",
			"Spring 운영 경험 요약",
			"https://toss.tech/spring"
		);
		PublicPostDetailResponse detail = new PublicPostDetailResponse(
			1L,
			"toss-spring",
			"Spring 운영 경험",
			PublicCompanySummaryResponse.of("toss", "토스"),
			PublicSourceSummaryResponse.of("토스 기술 블로그", "https://toss.tech"),
			Instant.parse("2026-05-02T01:00:00Z"),
			null,
			null,
			"ready",
			PublicPostSummaryResponse.of("요약", null),
			"https://toss.tech/spring",
			"AI가 원문을 바탕으로 생성한 요약입니다."
		);
		PublicFilterMetadataResponse filters = PublicFilterMetadataResponse.of(null, null, null);

		assertThat(listItem.jobCategories()).isEmpty();
		assertThat(listItem.topicTags()).isEmpty();
		assertThat(detail.jobCategories()).isEmpty();
		assertThat(detail.topicTags()).isEmpty();
		assertThat(detail.summary().bullets()).isEmpty();
		assertThat(filters.companies()).isEmpty();
		assertThat(filters.jobs()).isEmpty();
		assertThat(filters.tags()).isEmpty();
	}
}

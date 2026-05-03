package com.techloghub.api.worker.collection;

import static com.techloghub.api.testsupport.TestTags.UNIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.techloghub.api.worker.batch.TechlogBatchProperties;
import com.techloghub.api.worker.normalization.CanonicalUrlResolver;
import com.techloghub.api.worker.normalization.FeedEntryNormalizer;

@Tag(UNIT)
class RomeFeedClientTests {

	private static final Instant COLLECTED_AT = Instant.parse("2026-05-02T11:00:00Z");

	private final TechlogBatchProperties properties = new TechlogBatchProperties();
	private final RomeFeedClient client = new RomeFeedClient(
		properties,
		new FeedEntryNormalizer(new CanonicalUrlResolver()),
		Clock.fixed(COLLECTED_AT, ZoneOffset.UTC)
	);

	@Test
	@DisplayName("RSS 2.0 feed를 수집 후보 목록으로 파싱한다")
	void parse_rssFeed_returnsCandidates() {
		List<FeedEntryCandidate> candidates = client.parse(openFixture("feeds/sample-rss.xml"), COLLECTED_AT);

		assertThat(candidates).hasSize(2);
		assertThat(candidates.get(0).title()).isEqualTo("Spring Boot 운영 경험");
		assertThat(candidates.get(0).originUrl()).isEqualTo("https://toss.tech/article/spring-boot?utm_source=rss");
		assertThat(candidates.get(0).canonicalUrl()).isEqualTo("https://toss.tech/article/spring-boot?utm_source=rss");
		assertThat(candidates.get(0).publishedAt()).isEqualTo(Instant.parse("2026-05-02T09:30:00Z"));
		assertThat(candidates.get(0).summaryText()).isEqualTo("Spring Boot 운영 경험을 정리합니다.");
	}

	@Test
	@DisplayName("Atom feed의 canonical link를 우선 canonical URL로 사용한다")
	void parse_atomFeed_usesCanonicalLink() {
		List<FeedEntryCandidate> candidates = client.parse(openFixture("feeds/sample-atom.xml"), COLLECTED_AT);

		assertThat(candidates).hasSize(1);
		assertThat(candidates.get(0).title()).isEqualTo("JVM GC 튜닝 사례");
		assertThat(candidates.get(0).originUrl()).isEqualTo("https://d2.naver.com/helloworld/1234");
		assertThat(candidates.get(0).canonicalUrl()).isEqualTo("https://d2.naver.com/helloworld/1234");
		assertThat(candidates.get(0).publishedAt()).isEqualTo(Instant.parse("2026-05-02T10:20:00Z"));
	}

	@Test
	@DisplayName("깨진 feed XML은 FeedFetchException으로 변환한다")
	void parse_malformedFeed_throwsFeedFetchException() {
		assertThatThrownBy(() -> client.parse(openFixture("feeds/malformed-feed.xml"), COLLECTED_AT))
			.isInstanceOf(FeedFetchException.class)
			.hasMessageContaining("failed to parse feed");
	}

	private static InputStream openFixture(String path) {
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		if (inputStream == null) {
			throw new IllegalArgumentException("fixture not found: " + path);
		}
		return inputStream;
	}
}

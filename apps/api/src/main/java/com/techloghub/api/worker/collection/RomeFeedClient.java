package com.techloghub.api.worker.collection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.techloghub.api.common.util.UrlSupport;
import com.techloghub.api.worker.batch.TechlogBatchProperties;
import com.techloghub.api.worker.normalization.FeedEntryNormalizer;

/**
 * ROME 기반 RSS/Atom feed client 구현체다.
 */
@Component
public class RomeFeedClient implements FeedClient {

	private final TechlogBatchProperties properties;
	private final FeedEntryNormalizer feedEntryNormalizer;
	private final Clock clock;
	private final HttpClient httpClient;

	public RomeFeedClient(
		TechlogBatchProperties properties,
		FeedEntryNormalizer feedEntryNormalizer,
		Clock clock
	) {
		this.properties = properties;
		this.feedEntryNormalizer = feedEntryNormalizer;
		this.clock = clock;
		this.httpClient = HttpClient.newBuilder()
			.connectTimeout(properties.getFeedConnectTimeout())
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
	}

	@Override
	public List<FeedEntryCandidate> fetch(String feedUrl) {
		URI uri = UrlSupport.parseHttpUri(feedUrl, "feedUrl");
		HttpRequest request = HttpRequest.newBuilder(uri)
			.timeout(properties.getFeedReadTimeout())
			.header("User-Agent", properties.getUserAgent())
			.GET()
			.build();

		try {
			HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new FeedFetchException("feed response status is not 2xx: " + response.statusCode());
			}
			if (response.body().length > properties.getMaxFeedResponseBytes()) {
				throw new FeedFetchException("feed response body is too large: " + response.body().length);
			}
			try (InputStream body = new ByteArrayInputStream(response.body())) {
				return parse(body, clock.instant());
			}
		} catch (FeedFetchException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new FeedFetchException("failed to fetch feed: " + uri, exception);
		}
	}

	List<FeedEntryCandidate> parse(InputStream inputStream, Instant collectedAt) {
		try {
			SyndFeed feed = new SyndFeedInput().build(new XmlReader(inputStream));
			return feed.getEntries()
				.stream()
				.limit(properties.getMaxEntriesPerSource())
				.map(entry -> toCandidate(entry, collectedAt))
				.flatMap(Optional::stream)
				.toList();
		} catch (Exception exception) {
			throw new FeedFetchException("failed to parse feed", exception);
		}
	}

	private Optional<FeedEntryCandidate> toCandidate(SyndEntry entry, Instant collectedAt) {
		return feedEntryNormalizer.normalize(
			entry.getTitle(),
			extractOriginLink(entry),
			extractCanonicalLink(entry),
			toInstant(entry),
			extractSummaryText(entry),
			collectedAt
		);
	}

	private static String extractOriginLink(SyndEntry entry) {
		if (entry.getLink() != null && !entry.getLink().isBlank()) {
			return entry.getLink();
		}
		return entry.getLinks()
			.stream()
			.filter(link -> link.getHref() != null && !link.getHref().isBlank())
			.filter(link -> link.getRel() == null || link.getRel().isBlank() || "alternate".equalsIgnoreCase(link.getRel()))
			.map(SyndLink::getHref)
			.findFirst()
			.orElse(null);
	}

	private static String extractCanonicalLink(SyndEntry entry) {
		return entry.getLinks()
			.stream()
			.filter(link -> "canonical".equalsIgnoreCase(link.getRel()))
			.map(SyndLink::getHref)
			.filter(href -> href != null && !href.isBlank())
			.findFirst()
			.orElse(null);
	}

	private static Instant toInstant(SyndEntry entry) {
		if (entry.getPublishedDate() != null) {
			return entry.getPublishedDate().toInstant();
		}
		if (entry.getUpdatedDate() != null) {
			return entry.getUpdatedDate().toInstant();
		}
		return null;
	}

	private static String extractSummaryText(SyndEntry entry) {
		SyndContent description = entry.getDescription();
		if (description != null && description.getValue() != null) {
			return description.getValue();
		}
		return entry.getContents()
			.stream()
			.map(SyndContent::getValue)
			.filter(value -> value != null && !value.isBlank())
			.findFirst()
			.orElse(null);
	}
}

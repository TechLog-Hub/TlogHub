package com.techloghub.api.worker.normalization;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.techloghub.api.common.util.StringNormalizer;
import com.techloghub.api.common.util.UrlSupport;
import com.techloghub.api.worker.collection.FeedEntryCandidate;

import lombok.RequiredArgsConstructor;

/**
 * feed parser 결과를 저장 가능한 entry 후보로 정규화한다.
 */
@Component
@RequiredArgsConstructor
public class FeedEntryNormalizer {

	private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>");

	private final CanonicalUrlResolver canonicalUrlResolver;

	public Optional<FeedEntryCandidate> normalize(
		String title,
		String originUrl,
		String canonicalUrl,
		Instant publishedAt,
		String summaryText,
		Instant collectedAt
	) {
		String normalizedTitle = StringNormalizer.normalizeWhitespaceToNull(title);
		String normalizedOriginUrl = normalizeUrlOrNull(originUrl);
		if (normalizedTitle == null || normalizedOriginUrl == null) {
			return Optional.empty();
		}
		String normalizedCanonicalUrl = canonicalUrlResolver.resolve(canonicalUrl, normalizedOriginUrl);
		Instant normalizedPublishedAt = publishedAt == null ? collectedAt : publishedAt;
		return Optional.of(new FeedEntryCandidate(
			normalizedTitle,
			normalizedOriginUrl,
			normalizedCanonicalUrl,
			normalizedPublishedAt,
			stripHtml(summaryText)
		));
	}

	private static String normalizeUrlOrNull(String value) {
		try {
			return UrlSupport.normalizeUrl(value);
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

	private static String stripHtml(String value) {
		String normalized = StringNormalizer.normalizeWhitespaceToNull(value);
		if (normalized == null) {
			return null;
		}
		return StringNormalizer.normalizeWhitespaceToNull(HTML_TAG.matcher(normalized).replaceAll(" "));
	}
}

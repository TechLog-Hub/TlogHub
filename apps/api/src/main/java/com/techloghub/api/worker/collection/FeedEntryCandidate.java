package com.techloghub.api.worker.collection;

import static com.techloghub.api.common.domain.DomainGuard.requireNonBlank;
import static com.techloghub.api.common.domain.DomainGuard.requireNonNull;

import java.time.Instant;

import com.techloghub.api.common.util.StringNormalizer;

/**
 * RSS/Atom entry를 내부 저장에 맞게 정규화한 후보 값이다.
 */
public record FeedEntryCandidate(
	String title,
	String originUrl,
	String canonicalUrl,
	Instant publishedAt,
	String summaryText
) {

	public FeedEntryCandidate {
		title = requireNonBlank(title, "title");
		originUrl = requireNonBlank(originUrl, "originUrl");
		canonicalUrl = requireNonBlank(canonicalUrl, "canonicalUrl");
		publishedAt = requireNonNull(publishedAt, "publishedAt");
		summaryText = StringNormalizer.normalizeWhitespaceToNull(summaryText);
	}
}

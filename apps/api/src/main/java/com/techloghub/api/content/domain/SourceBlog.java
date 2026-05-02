package com.techloghub.api.content.domain;

import static com.techloghub.api.common.domain.DomainGuard.normalizeBlankToNull;
import static com.techloghub.api.common.domain.DomainGuard.requireBoolean;
import static com.techloghub.api.common.domain.DomainGuard.requireNonBlank;
import static com.techloghub.api.common.domain.DomainGuard.requireNonNull;
import static com.techloghub.api.common.domain.DomainGuard.requireTrimmedNonBlank;

import java.time.Instant;

import com.techloghub.api.common.domain.BaseEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "source_blog",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_source_blog_feed_url", columnNames = "feed_url")
	},
	indexes = {
		@Index(name = "idx_source_blog_company_id", columnList = "company_id"),
		@Index(name = "idx_source_blog_status", columnList = "status")
	}
)
public class SourceBlog extends BaseEntity {

	private static final int NAME_MAX_LENGTH = 150;
	private static final int URL_MAX_LENGTH = 500;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Column(name = "homepage_url", nullable = false, length = 500)
	private String homepageUrl;

	@Column(name = "feed_url", length = 500)
	private String feedUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "source_type", nullable = false, length = 30)
	private SourceType sourceType;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private SourceBlogStatus status;

	@Column(name = "review_reason", columnDefinition = "text")
	private String reviewReason;

	@Column(name = "last_collected_at")
	private Instant lastCollectedAt;

	private SourceBlog(Company company, String name, String homepageUrl, String feedUrl, SourceType sourceType) {
		this.company = requireNonNull(company, "company");
		this.name = requireTrimmedNonBlank(name, "name");
		this.homepageUrl = requireTrimmedNonBlank(homepageUrl, "homepageUrl");
		this.feedUrl = normalizeBlankToNull(feedUrl);
		this.sourceType = requireNonNull(sourceType, "sourceType");
		this.status = SourceBlogStatus.PROPOSED;
		validateNameLength(this.name);
		validateUrlLength(this.homepageUrl);
		validateUrlLengthOrNull(this.feedUrl);
	}

	public static SourceBlog propose(Company company, String name, String homepageUrl, String feedUrl, SourceType sourceType) {
		return new SourceBlog(company, name, homepageUrl, feedUrl, sourceType);
	}

	public void approve() {
		validateFeedUrlForRssAtom();
		this.status = SourceBlogStatus.APPROVED;
		this.reviewReason = null;
	}

	public void pause(String reason) {
		this.status = SourceBlogStatus.PAUSED;
		this.reviewReason = requireNonBlank(reason, "reason");
	}

	public void reject(String reason) {
		this.status = SourceBlogStatus.REJECTED;
		this.reviewReason = requireNonBlank(reason, "reason");
	}

	public void updateFeedUrl(String feedUrl) {
		this.feedUrl = normalizeBlankToNull(feedUrl);
		validateFeedUrlForRssAtom();
		validateUrlLengthOrNull(this.feedUrl);
		this.reviewReason = null;
	}

	public void updateName(String name) {
		this.name = requireTrimmedNonBlank(name, "name");
		validateNameLength(this.name);
	}

	public void updateHomepageUrl(String homepageUrl) {
		this.homepageUrl = requireTrimmedNonBlank(homepageUrl, "homepageUrl");
		validateUrlLength(this.homepageUrl);
	}

	public void markCollectedAt(Instant collectedAt) {
		this.lastCollectedAt = requireNonNull(collectedAt, "collectedAt");
	}

	private void validateFeedUrlForRssAtom() {
		if (sourceType == SourceType.RSS || sourceType == SourceType.ATOM) {
			requireBoolean(feedUrl != null && !feedUrl.isBlank(), "feedUrl is required for RSS or ATOM source");
		}
	}

	private static void validateNameLength(String value) {
		if (value.length() > NAME_MAX_LENGTH) {
			throw new IllegalArgumentException("name length must be <= " + NAME_MAX_LENGTH);
		}
	}

	private static void validateUrlLength(String value) {
		if (value.length() > URL_MAX_LENGTH) {
			throw new IllegalArgumentException("url length must be <= " + URL_MAX_LENGTH);
		}
	}

	private static void validateUrlLengthOrNull(String value) {
		if (value != null && value.length() > URL_MAX_LENGTH) {
			throw new IllegalArgumentException("url length must be <= " + URL_MAX_LENGTH);
		}
	}
}

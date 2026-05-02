package com.techloghub.api.content.domain;

import static com.techloghub.api.common.domain.DomainGuard.normalizeBlankToNull;
import static com.techloghub.api.common.domain.DomainGuard.requireNonBlank;
import static com.techloghub.api.common.domain.DomainGuard.requireNonNull;

import java.time.Instant;

import com.techloghub.api.common.domain.BaseEntity;

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

	protected SourceBlog() {
	}

	private SourceBlog(Company company, String name, String homepageUrl, String feedUrl, SourceType sourceType) {
		this.company = requireNonNull(company, "company");
		this.name = requireNonBlank(name, "name");
		this.homepageUrl = requireNonBlank(homepageUrl, "homepageUrl");
		this.feedUrl = normalizeBlankToNull(feedUrl);
		this.sourceType = requireNonNull(sourceType, "sourceType");
		this.status = SourceBlogStatus.PROPOSED;
	}

	public static SourceBlog propose(Company company, String name, String homepageUrl, String feedUrl, SourceType sourceType) {
		return new SourceBlog(company, name, homepageUrl, feedUrl, sourceType);
	}

	public void approve() {
		if (feedUrl == null && (sourceType == SourceType.RSS || sourceType == SourceType.ATOM)) {
			throw new IllegalStateException("feedUrl is required for RSS or ATOM source");
		}
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

	public void markCollectedAt(Instant collectedAt) {
		this.lastCollectedAt = requireNonNull(collectedAt, "collectedAt");
	}

	public Long getId() {
		return id;
	}

	public Company getCompany() {
		return company;
	}

	public String getName() {
		return name;
	}

	public String getHomepageUrl() {
		return homepageUrl;
	}

	public String getFeedUrl() {
		return feedUrl;
	}

	public SourceType getSourceType() {
		return sourceType;
	}

	public SourceBlogStatus getStatus() {
		return status;
	}

	public String getReviewReason() {
		return reviewReason;
	}

	public Instant getLastCollectedAt() {
		return lastCollectedAt;
	}
}

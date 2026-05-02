package com.techloghub.api.content.domain;

import static com.techloghub.api.common.domain.DomainGuard.normalizeBlankToNull;
import static com.techloghub.api.common.domain.DomainGuard.requireMaxSize;
import static com.techloghub.api.common.domain.DomainGuard.requireNonBlank;
import static com.techloghub.api.common.domain.DomainGuard.requireNonNull;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "archived_post",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_archived_post_slug", columnNames = "slug"),
		@UniqueConstraint(name = "uk_archived_post_canonical_fingerprint", columnNames = "canonical_fingerprint")
	},
	indexes = {
		@Index(name = "idx_archived_post_company_published", columnList = "company_id, published_at"),
		@Index(name = "idx_archived_post_source_blog_id", columnList = "source_blog_id"),
		@Index(name = "idx_archived_post_visibility_state", columnList = "visibility_state"),
		@Index(name = "idx_archived_post_processing_state", columnList = "processing_state")
	}
)
public class ArchivedPost extends BaseEntity {

	private static final int MAX_JOB_CATEGORY_COUNT = 3;
	private static final int MAX_TOPIC_TAG_COUNT = 8;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "source_blog_id", nullable = false)
	private SourceBlog sourceBlog;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Column(name = "slug", nullable = false, length = 180)
	private String slug;

	@Column(name = "title", nullable = false, length = 500)
	private String title;

	@Column(name = "origin_url", nullable = false, length = 1_000)
	private String originUrl;

	@Column(name = "canonical_url", nullable = false, length = 1_000)
	private String canonicalUrl;

	@Column(name = "canonical_fingerprint", nullable = false, length = 128)
	private String canonicalFingerprint;

	@Column(name = "published_at", nullable = false)
	private Instant publishedAt;

	@Column(name = "collected_at", nullable = false)
	private Instant collectedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "processing_state", nullable = false, length = 30)
	private ProcessingState processingState;

	@Enumerated(EnumType.STRING)
	@Column(name = "visibility_state", nullable = false, length = 30)
	private VisibilityState visibilityState;

	@Column(name = "review_reason", columnDefinition = "text")
	private String reviewReason;

	@ManyToMany
	@JoinTable(
		name = "archived_post_job_category",
		joinColumns = @JoinColumn(name = "archived_post_id"),
		inverseJoinColumns = @JoinColumn(name = "job_category_id"),
		uniqueConstraints = {
			@UniqueConstraint(
				name = "uk_archived_post_job_category",
				columnNames = {"archived_post_id", "job_category_id"}
			)
		}
	)
	private Set<JobCategory> jobCategories = new LinkedHashSet<>();

	@ManyToMany
	@JoinTable(
		name = "archived_post_topic_tag",
		joinColumns = @JoinColumn(name = "archived_post_id"),
		inverseJoinColumns = @JoinColumn(name = "topic_tag_id"),
		uniqueConstraints = {
			@UniqueConstraint(
				name = "uk_archived_post_topic_tag",
				columnNames = {"archived_post_id", "topic_tag_id"}
			)
		}
	)
	private Set<TopicTag> topicTags = new LinkedHashSet<>();

	protected ArchivedPost() {
	}

	private ArchivedPost(
		SourceBlog sourceBlog,
		String slug,
		String title,
		String originUrl,
		String canonicalUrl,
		String canonicalFingerprint,
		Instant publishedAt,
		Instant collectedAt
	) {
		this.sourceBlog = requireNonNull(sourceBlog, "sourceBlog");
		this.company = sourceBlog.getCompany();
		this.slug = requireNonBlank(slug, "slug");
		this.title = requireNonBlank(title, "title");
		this.originUrl = requireNonBlank(originUrl, "originUrl");
		this.canonicalUrl = requireNonBlank(canonicalUrl, "canonicalUrl");
		this.canonicalFingerprint = requireNonBlank(canonicalFingerprint, "canonicalFingerprint");
		this.publishedAt = requireNonNull(publishedAt, "publishedAt");
		this.collectedAt = requireNonNull(collectedAt, "collectedAt");
		this.processingState = ProcessingState.COLLECTED;
		this.visibilityState = VisibilityState.DRAFT;
	}

	public static ArchivedPost collect(
		SourceBlog sourceBlog,
		String slug,
		String title,
		String originUrl,
		String canonicalUrl,
		String canonicalFingerprint,
		Instant publishedAt,
		Instant collectedAt
	) {
		return new ArchivedPost(sourceBlog, slug, title, originUrl, canonicalUrl, canonicalFingerprint, publishedAt, collectedAt);
	}

	public void replaceJobCategories(Collection<JobCategory> categories) {
		requireMaxSize(categories, MAX_JOB_CATEGORY_COUNT, "jobCategories");
		this.jobCategories = new LinkedHashSet<>(categories);
	}

	public void replaceTopicTags(Collection<TopicTag> tags) {
		requireMaxSize(tags, MAX_TOPIC_TAG_COUNT, "topicTags");
		this.topicTags = new LinkedHashSet<>(tags);
	}

	public void markClassified() {
		this.processingState = ProcessingState.CLASSIFIED;
		this.reviewReason = null;
	}

	public void requireReview(String reason) {
		this.processingState = ProcessingState.REVIEW_REQUIRED;
		this.reviewReason = requireNonBlank(reason, "reason");
	}

	public void publish() {
		if (processingState == ProcessingState.REVIEW_REQUIRED) {
			throw new IllegalStateException("review required post cannot be published");
		}
		this.visibilityState = VisibilityState.PUBLISHED;
	}

	public void hide(String reason) {
		this.visibilityState = VisibilityState.HIDDEN;
		this.reviewReason = normalizeBlankToNull(reason);
	}

	public void block(String reason) {
		this.visibilityState = VisibilityState.BLOCKED;
		this.reviewReason = requireNonBlank(reason, "reason");
	}

	public Long getId() {
		return id;
	}

	public SourceBlog getSourceBlog() {
		return sourceBlog;
	}

	public Company getCompany() {
		return company;
	}

	public String getSlug() {
		return slug;
	}

	public String getTitle() {
		return title;
	}

	public String getOriginUrl() {
		return originUrl;
	}

	public String getCanonicalUrl() {
		return canonicalUrl;
	}

	public String getCanonicalFingerprint() {
		return canonicalFingerprint;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}

	public Instant getCollectedAt() {
		return collectedAt;
	}

	public ProcessingState getProcessingState() {
		return processingState;
	}

	public VisibilityState getVisibilityState() {
		return visibilityState;
	}

	public String getReviewReason() {
		return reviewReason;
	}

	public Set<JobCategory> getJobCategories() {
		return Set.copyOf(jobCategories);
	}

	public Set<TopicTag> getTopicTags() {
		return Set.copyOf(topicTags);
	}
}

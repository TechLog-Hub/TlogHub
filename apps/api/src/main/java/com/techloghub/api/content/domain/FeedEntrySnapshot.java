package com.techloghub.api.content.domain;

import static com.techloghub.api.common.domain.DomainGuard.normalizeBlankToNull;
import static com.techloghub.api.common.domain.DomainGuard.requireNonBlank;
import static com.techloghub.api.common.domain.DomainGuard.requireNonNull;

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
	name = "feed_entry_snapshot",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_feed_entry_snapshot_source_url",
			columnNames = {"source_blog_id", "origin_url"}
		)
	},
	indexes = {
		@Index(name = "idx_feed_entry_snapshot_archived_post_id", columnList = "archived_post_id"),
		@Index(name = "idx_feed_entry_snapshot_collection_run_id", columnList = "collection_run_id"),
		@Index(name = "idx_feed_entry_snapshot_fingerprint", columnList = "canonical_fingerprint")
	}
)
public class FeedEntrySnapshot extends BaseEntity {

	private static final int TITLE_MAX_LENGTH = 500;
	private static final int URL_MAX_LENGTH = 1_000;
	private static final int FINGERPRINT_MAX_LENGTH = 128;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "archived_post_id", nullable = false)
	private ArchivedPost archivedPost;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "source_blog_id", nullable = false)
	private SourceBlog sourceBlog;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "collection_run_id", nullable = false)
	private CollectionRun collectionRun;

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

	@Column(name = "summary_text", columnDefinition = "text")
	private String summaryText;

	@Enumerated(EnumType.STRING)
	@Column(name = "duplicate_type", nullable = false, length = 30)
	private DuplicateType duplicateType;

	private FeedEntrySnapshot(
		ArchivedPost archivedPost,
		SourceBlog sourceBlog,
		CollectionRun collectionRun,
		String title,
		String originUrl,
		String canonicalUrl,
		String canonicalFingerprint,
		Instant publishedAt,
		Instant collectedAt,
		String summaryText,
		DuplicateType duplicateType
	) {
		this.archivedPost = requireNonNull(archivedPost, "archivedPost");
		this.sourceBlog = requireNonNull(sourceBlog, "sourceBlog");
		this.collectionRun = requireNonNull(collectionRun, "collectionRun");
		this.title = requireNonBlank(title, "title");
		this.originUrl = requireNonBlank(originUrl, "originUrl");
		this.canonicalUrl = requireNonBlank(canonicalUrl, "canonicalUrl");
		this.canonicalFingerprint = requireNonBlank(canonicalFingerprint, "canonicalFingerprint");
		this.publishedAt = requireNonNull(publishedAt, "publishedAt");
		this.collectedAt = requireNonNull(collectedAt, "collectedAt");
		this.summaryText = normalizeBlankToNull(summaryText);
		this.duplicateType = requireNonNull(duplicateType, "duplicateType");
		validateLength(this.title, TITLE_MAX_LENGTH, "title");
		validateLength(this.originUrl, URL_MAX_LENGTH, "originUrl");
		validateLength(this.canonicalUrl, URL_MAX_LENGTH, "canonicalUrl");
		validateLength(this.canonicalFingerprint, FINGERPRINT_MAX_LENGTH, "canonicalFingerprint");
	}

	public static FeedEntrySnapshot create(
		ArchivedPost archivedPost,
		SourceBlog sourceBlog,
		CollectionRun collectionRun,
		String title,
		String originUrl,
		String canonicalUrl,
		String canonicalFingerprint,
		Instant publishedAt,
		Instant collectedAt,
		String summaryText,
		DuplicateType duplicateType
	) {
		return new FeedEntrySnapshot(
			archivedPost,
			sourceBlog,
			collectionRun,
			title,
			originUrl,
			canonicalUrl,
			canonicalFingerprint,
			publishedAt,
			collectedAt,
			summaryText,
			duplicateType
		);
	}

	private static void validateLength(String value, int maxLength, String fieldName) {
		if (value.length() > maxLength) {
			throw new IllegalArgumentException(fieldName + " length must be <= " + maxLength);
		}
	}
}

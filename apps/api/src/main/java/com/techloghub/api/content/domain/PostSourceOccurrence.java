package com.techloghub.api.content.domain;

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
	name = "post_source_occurrence",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_post_source_occurrence_source_url",
			columnNames = {"source_blog_id", "origin_url"}
		)
	},
	indexes = {
		@Index(name = "idx_post_source_occurrence_archived_post_id", columnList = "archived_post_id")
	}
)
public class PostSourceOccurrence extends BaseEntity {

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

	@Column(name = "origin_url", nullable = false, length = 1_000)
	private String originUrl;

	@Column(name = "published_at", nullable = false)
	private Instant publishedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "duplicate_type", nullable = false, length = 30)
	private DuplicateType duplicateType;

	protected PostSourceOccurrence() {
	}

	private PostSourceOccurrence(
		ArchivedPost archivedPost,
		SourceBlog sourceBlog,
		String originUrl,
		Instant publishedAt,
		DuplicateType duplicateType
	) {
		this.archivedPost = requireNonNull(archivedPost, "archivedPost");
		this.sourceBlog = requireNonNull(sourceBlog, "sourceBlog");
		this.originUrl = requireNonBlank(originUrl, "originUrl");
		this.publishedAt = requireNonNull(publishedAt, "publishedAt");
		this.duplicateType = requireNonNull(duplicateType, "duplicateType");
	}

	public static PostSourceOccurrence original(ArchivedPost archivedPost, SourceBlog sourceBlog, String originUrl, Instant publishedAt) {
		return new PostSourceOccurrence(archivedPost, sourceBlog, originUrl, publishedAt, DuplicateType.ORIGINAL);
	}

	public static PostSourceOccurrence duplicate(
		ArchivedPost archivedPost,
		SourceBlog sourceBlog,
		String originUrl,
		Instant publishedAt,
		DuplicateType duplicateType
	) {
		if (duplicateType == DuplicateType.ORIGINAL) {
			throw new IllegalArgumentException("duplicateType must be duplicate type");
		}
		return new PostSourceOccurrence(archivedPost, sourceBlog, originUrl, publishedAt, duplicateType);
	}

	public Long getId() {
		return id;
	}

	public ArchivedPost getArchivedPost() {
		return archivedPost;
	}

	public SourceBlog getSourceBlog() {
		return sourceBlog;
	}

	public String getOriginUrl() {
		return originUrl;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}

	public DuplicateType getDuplicateType() {
		return duplicateType;
	}
}

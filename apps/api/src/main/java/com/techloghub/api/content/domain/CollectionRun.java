package com.techloghub.api.content.domain;

import static com.techloghub.api.common.domain.DomainGuard.normalizeBlankToNull;
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

@Entity
@Table(
	name = "collection_run",
	indexes = {
		@Index(name = "idx_collection_run_source_blog_id", columnList = "source_blog_id"),
		@Index(name = "idx_collection_run_status", columnList = "status")
	}
)
public class CollectionRun extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "source_blog_id", nullable = false)
	private SourceBlog sourceBlog;

	@Column(name = "started_at", nullable = false)
	private Instant startedAt;

	@Column(name = "finished_at")
	private Instant finishedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private CollectionRunStatus status;

	@Column(name = "collected_count", nullable = false)
	private int collectedCount;

	@Column(name = "new_post_count", nullable = false)
	private int newPostCount;

	@Column(name = "failure_reason", columnDefinition = "text")
	private String failureReason;

	protected CollectionRun() {
	}

	private CollectionRun(SourceBlog sourceBlog, Instant startedAt) {
		this.sourceBlog = requireNonNull(sourceBlog, "sourceBlog");
		this.startedAt = requireNonNull(startedAt, "startedAt");
		this.status = CollectionRunStatus.RUNNING;
	}

	public static CollectionRun start(SourceBlog sourceBlog, Instant startedAt) {
		return new CollectionRun(sourceBlog, startedAt);
	}

	public void succeed(Instant finishedAt, int collectedCount, int newPostCount) {
		this.finishedAt = requireNonNull(finishedAt, "finishedAt");
		this.collectedCount = collectedCount;
		this.newPostCount = newPostCount;
		this.failureReason = null;
		this.status = CollectionRunStatus.SUCCESS;
	}

	public void fail(Instant finishedAt, String failureReason) {
		this.finishedAt = requireNonNull(finishedAt, "finishedAt");
		this.failureReason = normalizeBlankToNull(failureReason);
		this.status = CollectionRunStatus.FAILED;
	}

	public Long getId() {
		return id;
	}

	public SourceBlog getSourceBlog() {
		return sourceBlog;
	}

	public Instant getStartedAt() {
		return startedAt;
	}

	public Instant getFinishedAt() {
		return finishedAt;
	}

	public CollectionRunStatus getStatus() {
		return status;
	}

	public int getCollectedCount() {
		return collectedCount;
	}

	public int getNewPostCount() {
		return newPostCount;
	}

	public String getFailureReason() {
		return failureReason;
	}
}

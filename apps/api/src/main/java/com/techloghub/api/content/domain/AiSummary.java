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
	name = "ai_summary",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_ai_summary_post_version", columnNames = {"archived_post_id", "version_no"})
	},
	indexes = {
		@Index(name = "idx_ai_summary_current", columnList = "archived_post_id, is_current")
	}
)
public class AiSummary extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "archived_post_id", nullable = false)
	private ArchivedPost archivedPost;

	@Column(name = "version_no", nullable = false)
	private int versionNo;

	@Enumerated(EnumType.STRING)
	@Column(name = "summary_state", nullable = false, length = 30)
	private SummaryState summaryState;

	@Column(name = "headline", length = 500)
	private String headline;

	@Column(name = "bullets_json", columnDefinition = "text")
	private String bulletsJson;

	@Column(name = "model_name", length = 100)
	private String modelName;

	@Column(name = "prompt_version", length = 100)
	private String promptVersion;

	@Column(name = "generated_at")
	private Instant generatedAt;

	@Column(name = "is_current", nullable = false)
	private boolean current;

	@Column(name = "failure_reason", columnDefinition = "text")
	private String failureReason;

	protected AiSummary() {
	}

	private AiSummary(ArchivedPost archivedPost, int versionNo, SummaryState summaryState) {
		this.archivedPost = requireNonNull(archivedPost, "archivedPost");
		if (versionNo < 1) {
			throw new IllegalArgumentException("versionNo must be positive");
		}
		this.versionNo = versionNo;
		this.summaryState = requireNonNull(summaryState, "summaryState");
		this.current = true;
	}

	public static AiSummary pending(ArchivedPost archivedPost, int versionNo) {
		return new AiSummary(archivedPost, versionNo, SummaryState.PENDING);
	}

	public static AiSummary ready(
		ArchivedPost archivedPost,
		int versionNo,
		String headline,
		String bulletsJson,
		String modelName,
		String promptVersion,
		Instant generatedAt
	) {
		AiSummary summary = new AiSummary(archivedPost, versionNo, SummaryState.READY);
		summary.headline = requireNonBlank(headline, "headline");
		summary.bulletsJson = requireNonBlank(bulletsJson, "bulletsJson");
		summary.modelName = requireNonBlank(modelName, "modelName");
		summary.promptVersion = requireNonBlank(promptVersion, "promptVersion");
		summary.generatedAt = requireNonNull(generatedAt, "generatedAt");
		return summary;
	}

	public static AiSummary failed(ArchivedPost archivedPost, int versionNo, String failureReason) {
		AiSummary summary = new AiSummary(archivedPost, versionNo, SummaryState.FAILED);
		summary.failureReason = requireNonBlank(failureReason, "failureReason");
		return summary;
	}

	public void hide(String reason) {
		this.summaryState = SummaryState.HIDDEN;
		this.failureReason = normalizeBlankToNull(reason);
	}

	public void retire() {
		this.current = false;
	}

	public Long getId() {
		return id;
	}

	public ArchivedPost getArchivedPost() {
		return archivedPost;
	}

	public int getVersionNo() {
		return versionNo;
	}

	public SummaryState getSummaryState() {
		return summaryState;
	}

	public String getHeadline() {
		return headline;
	}

	public String getBulletsJson() {
		return bulletsJson;
	}

	public String getModelName() {
		return modelName;
	}

	public String getPromptVersion() {
		return promptVersion;
	}

	public Instant getGeneratedAt() {
		return generatedAt;
	}

	public boolean isCurrent() {
		return current;
	}

	public String getFailureReason() {
		return failureReason;
	}
}

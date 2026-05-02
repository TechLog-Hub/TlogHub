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
	name = "ai_summary",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_ai_summary_post_version", columnNames = {"archived_post_id", "version_no"})
	},
	indexes = {
		@Index(name = "idx_ai_summary_current", columnList = "archived_post_id, is_current")
	}
)
public class AiSummary extends BaseEntity {

	private static final int HEADLINE_MAX_LENGTH = 500;
	private static final int MODEL_NAME_MAX_LENGTH = 100;
	private static final int PROMPT_VERSION_MAX_LENGTH = 100;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "archived_post_id", nullable = false)
	private ArchivedPost archivedPost;

	@Column(name = "version_no", nullable = false)
	private int summaryVersion;

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

	private AiSummary(ArchivedPost archivedPost, int version, SummaryState summaryState) {
		this.archivedPost = requireNonNull(archivedPost, "archivedPost");
		if (version < 1) {
			throw new IllegalArgumentException("version must be positive");
		}
		this.summaryVersion = version;
		this.summaryState = requireNonNull(summaryState, "summaryState");
		this.current = true;
	}

	public static AiSummary pending(ArchivedPost archivedPost, int version) {
		return new AiSummary(archivedPost, version, SummaryState.PENDING);
	}

	public static AiSummary ready(
		ArchivedPost archivedPost,
		int version,
		String headline,
		String bulletsJson,
		String modelName,
		String promptVersion,
		Instant generatedAt
	) {
		AiSummary summary = new AiSummary(archivedPost, version, SummaryState.READY);
		summary.headline = requireNonBlank(headline, "headline");
		summary.bulletsJson = requireNonBlank(bulletsJson, "bulletsJson");
		summary.modelName = requireNonBlank(modelName, "modelName");
		summary.promptVersion = requireNonBlank(promptVersion, "promptVersion");
		summary.generatedAt = requireNonNull(generatedAt, "generatedAt");
		validateHeadlineLength(summary.headline);
		validateModelNameLength(summary.modelName);
		validatePromptVersionLength(summary.promptVersion);
		return summary;
	}

	public static AiSummary failed(ArchivedPost archivedPost, int version, String failureReason) {
		AiSummary summary = new AiSummary(archivedPost, version, SummaryState.FAILED);
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

	public void markReady(
		String headline,
		String bulletsJson,
		String modelName,
		String promptVersion,
		Instant generatedAt
	) {
		this.summaryState = SummaryState.READY;
		this.headline = requireNonBlank(headline, "headline");
		this.bulletsJson = requireNonBlank(bulletsJson, "bulletsJson");
		this.modelName = requireNonBlank(modelName, "modelName");
		this.promptVersion = requireNonBlank(promptVersion, "promptVersion");
		this.generatedAt = requireNonNull(generatedAt, "generatedAt");
		this.failureReason = null;
		this.current = true;
		validateHeadlineLength(this.headline);
		validateModelNameLength(this.modelName);
		validatePromptVersionLength(this.promptVersion);
	}

	private static void validateHeadlineLength(String value) {
		if (value.length() > HEADLINE_MAX_LENGTH) {
			throw new IllegalArgumentException("headline length must be <= " + HEADLINE_MAX_LENGTH);
		}
	}

	private static void validateModelNameLength(String value) {
		if (value.length() > MODEL_NAME_MAX_LENGTH) {
			throw new IllegalArgumentException("modelName length must be <= " + MODEL_NAME_MAX_LENGTH);
		}
	}

	private static void validatePromptVersionLength(String value) {
		if (value.length() > PROMPT_VERSION_MAX_LENGTH) {
			throw new IllegalArgumentException("promptVersion length must be <= " + PROMPT_VERSION_MAX_LENGTH);
		}
	}
}

package com.techloghub.api.content.domain;

import static com.techloghub.api.common.domain.DomainGuard.requireNonBlank;
import static com.techloghub.api.common.domain.DomainGuard.requireNonNull;

import com.techloghub.api.common.domain.BaseEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
	name = "topic_tag",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_topic_tag_slug", columnNames = "slug")
	}
)
public class TopicTag extends BaseEntity {

	private static final int SLUG_MAX_LENGTH = 100;
	private static final int LABEL_MAX_LENGTH = 100;
	private static final int NORMALIZED_LABEL_MAX_LENGTH = 100;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "slug", nullable = false, length = 100)
	private String slug;

	@Column(name = "label", nullable = false, length = 100)
	private String label;

	@Column(name = "normalized_label", nullable = false, length = 100)
	private String normalizedLabel;

	@Column(name = "active", nullable = false)
	private boolean active;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "merged_to_tag_id")
	private TopicTag mergedToTag;

	private TopicTag(String slug, String label, String normalizedLabel) {
		this.slug = requireNonBlank(slug, "slug");
		this.label = requireNonBlank(label, "label");
		this.normalizedLabel = requireNonBlank(normalizedLabel, "normalizedLabel");
		this.active = true;
		validateSlugLength(this.slug);
		validateLabelLength(this.label);
		validateNormalizedLabelLength(this.normalizedLabel);
	}

	public static TopicTag create(String slug, String label, String normalizedLabel) {
		return new TopicTag(slug, label, normalizedLabel);
	}

	public void changeLabel(String label, String normalizedLabel) {
		this.label = requireNonBlank(label, "label");
		this.normalizedLabel = requireNonBlank(normalizedLabel, "normalizedLabel");
		validateLabelLength(this.label);
		validateNormalizedLabelLength(this.normalizedLabel);
	}

	public void mergeTo(TopicTag targetTag) {
		requireNonNull(targetTag, "targetTag");
		if (this == targetTag) {
			throw new IllegalArgumentException("tag cannot be merged to itself");
		}
		this.mergedToTag = targetTag;
		this.active = false;
	}

	public void activate() {
		this.mergedToTag = null;
		this.active = true;
	}

	public void deactivate() {
		this.active = false;
	}

	private static void validateSlugLength(String value) {
		if (value.length() > SLUG_MAX_LENGTH) {
			throw new IllegalArgumentException("slug length must be <= " + SLUG_MAX_LENGTH);
		}
	}

	private static void validateLabelLength(String value) {
		if (value.length() > LABEL_MAX_LENGTH) {
			throw new IllegalArgumentException("label length must be <= " + LABEL_MAX_LENGTH);
		}
	}

	private static void validateNormalizedLabelLength(String value) {
		if (value.length() > NORMALIZED_LABEL_MAX_LENGTH) {
			throw new IllegalArgumentException(
				"normalizedLabel length must be <= " + NORMALIZED_LABEL_MAX_LENGTH
			);
		}
	}
}

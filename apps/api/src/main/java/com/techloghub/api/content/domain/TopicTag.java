package com.techloghub.api.content.domain;

import static com.techloghub.api.common.domain.DomainGuard.requireNonBlank;
import static com.techloghub.api.common.domain.DomainGuard.requireNonNull;

import com.techloghub.api.common.domain.BaseEntity;

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
@Table(
	name = "topic_tag",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_topic_tag_slug", columnNames = "slug")
	}
)
public class TopicTag extends BaseEntity {

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

	protected TopicTag() {
	}

	private TopicTag(String slug, String label, String normalizedLabel) {
		this.slug = requireNonBlank(slug, "slug");
		this.label = requireNonBlank(label, "label");
		this.normalizedLabel = requireNonBlank(normalizedLabel, "normalizedLabel");
		this.active = true;
	}

	public static TopicTag create(String slug, String label, String normalizedLabel) {
		return new TopicTag(slug, label, normalizedLabel);
	}

	public void changeLabel(String label, String normalizedLabel) {
		this.label = requireNonBlank(label, "label");
		this.normalizedLabel = requireNonBlank(normalizedLabel, "normalizedLabel");
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

	public Long getId() {
		return id;
	}

	public String getSlug() {
		return slug;
	}

	public String getLabel() {
		return label;
	}

	public String getNormalizedLabel() {
		return normalizedLabel;
	}

	public boolean isActive() {
		return active;
	}

	public TopicTag getMergedToTag() {
		return mergedToTag;
	}
}

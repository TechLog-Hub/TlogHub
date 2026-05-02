package com.techloghub.api.content.domain;

import static com.techloghub.api.common.domain.DomainGuard.requireNonBlank;

import com.techloghub.api.common.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "job_category",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_job_category_code", columnNames = "code")
	}
)
public class JobCategory extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "code", nullable = false, length = 50)
	private String code;

	@Column(name = "label_ko", nullable = false, length = 100)
	private String labelKo;

	@Column(name = "display_order", nullable = false)
	private int displayOrder;

	@Column(name = "active", nullable = false)
	private boolean active;

	protected JobCategory() {
	}

	private JobCategory(String code, String labelKo, int displayOrder) {
		this.code = requireNonBlank(code, "code");
		this.labelKo = requireNonBlank(labelKo, "labelKo");
		this.displayOrder = displayOrder;
		this.active = true;
	}

	public static JobCategory create(String code, String labelKo, int displayOrder) {
		return new JobCategory(code, labelKo, displayOrder);
	}

	public void changeLabel(String labelKo) {
		this.labelKo = requireNonBlank(labelKo, "labelKo");
	}

	public void activate() {
		this.active = true;
	}

	public void deactivate() {
		this.active = false;
	}

	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getLabelKo() {
		return labelKo;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public boolean isActive() {
		return active;
	}
}

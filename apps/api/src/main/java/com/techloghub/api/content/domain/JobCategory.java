package com.techloghub.api.content.domain;

import static com.techloghub.api.common.domain.DomainGuard.requireNonBlank;

import com.techloghub.api.common.domain.BaseEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "job_category",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_job_category_code", columnNames = "code")
	}
)
public class JobCategory extends BaseEntity {

	private static final int CODE_MAX_LENGTH = 50;
	private static final int LABEL_MAX_LENGTH = 100;

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

	private JobCategory(String code, String labelKo, int displayOrder) {
		this.code = requireNonBlank(code, "code");
		this.labelKo = requireNonBlank(labelKo, "labelKo");
		this.displayOrder = displayOrder;
		this.active = true;
		validateCodeLength(this.code);
		validateLabelLength(this.labelKo);
	}

	public static JobCategory create(String code, String labelKo, int displayOrder) {
		return new JobCategory(code, labelKo, displayOrder);
	}

	public void changeLabel(String labelKo) {
		this.labelKo = requireNonBlank(labelKo, "labelKo");
		validateLabelLength(this.labelKo);
	}

	public void changeDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public void activate() {
		this.active = true;
	}

	public void deactivate() {
		this.active = false;
	}

	private static void validateCodeLength(String value) {
		if (value.length() > CODE_MAX_LENGTH) {
			throw new IllegalArgumentException("code length must be <= " + CODE_MAX_LENGTH);
		}
	}

	private static void validateLabelLength(String value) {
		if (value.length() > LABEL_MAX_LENGTH) {
			throw new IllegalArgumentException("labelKo length must be <= " + LABEL_MAX_LENGTH);
		}
	}
}

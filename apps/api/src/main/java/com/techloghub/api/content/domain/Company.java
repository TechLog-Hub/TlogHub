package com.techloghub.api.content.domain;

import static com.techloghub.api.common.domain.DomainGuard.requireNonBlank;
import static com.techloghub.api.common.domain.DomainGuard.requireNonNull;

import com.techloghub.api.common.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "company",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_company_slug", columnNames = "slug")
	}
)
public class Company extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "slug", nullable = false, length = 100)
	private String slug;

	@Column(name = "name_ko", nullable = false, length = 100)
	private String nameKo;

	@Column(name = "name_en", nullable = false, length = 100)
	private String nameEn;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private CompanyStatus status;

	protected Company() {
	}

	private Company(String slug, String nameKo, String nameEn, CompanyStatus status) {
		this.slug = requireNonBlank(slug, "slug");
		this.nameKo = requireNonBlank(nameKo, "nameKo");
		this.nameEn = requireNonBlank(nameEn, "nameEn");
		this.status = requireNonNull(status, "status");
	}

	public static Company create(String slug, String nameKo, String nameEn) {
		return new Company(slug, nameKo, nameEn, CompanyStatus.ACTIVE);
	}

	public void rename(String nameKo, String nameEn) {
		this.nameKo = requireNonBlank(nameKo, "nameKo");
		this.nameEn = requireNonBlank(nameEn, "nameEn");
	}

	public void activate() {
		this.status = CompanyStatus.ACTIVE;
	}

	public void deactivate() {
		this.status = CompanyStatus.INACTIVE;
	}

	public Long getId() {
		return id;
	}

	public String getSlug() {
		return slug;
	}

	public String getNameKo() {
		return nameKo;
	}

	public String getNameEn() {
		return nameEn;
	}

	public CompanyStatus getStatus() {
		return status;
	}
}

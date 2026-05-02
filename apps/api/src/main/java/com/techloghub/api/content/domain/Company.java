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
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "company",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_company_slug", columnNames = "slug")
	},
	indexes = {
		@Index(name = "idx_company_status_name_ko", columnList = "status, name_ko")
	}
)
public class Company extends BaseEntity {

	private static final int SLUG_MAX_LENGTH = 100;
	private static final int NAME_MAX_LENGTH = 100;

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

	private Company(String slug, String nameKo, String nameEn, CompanyStatus status) {
		this.slug = requireNonBlank(slug, "slug");
		this.nameKo = requireNonBlank(nameKo, "nameKo");
		this.nameEn = requireNonBlank(nameEn, "nameEn");
		this.status = requireNonNull(status, "status");
		validateSlugLength(this.slug);
		validateNameLength(this.nameKo);
		validateNameLength(this.nameEn);
	}

	public static Company create(String slug, String nameKo, String nameEn) {
		validateSlugLength(slug);
		validateNameLength(nameKo);
		validateNameLength(nameEn);
		return new Company(slug, nameKo, nameEn, CompanyStatus.ACTIVE);
	}

	private static void validateSlugLength(String slug) {
		if (slug.length() > SLUG_MAX_LENGTH) {
			throw new IllegalArgumentException("slug length must be <= " + SLUG_MAX_LENGTH);
		}
	}

	private static void validateNameLength(String value) {
		if (value.length() > NAME_MAX_LENGTH) {
			throw new IllegalArgumentException("name length must be <= " + NAME_MAX_LENGTH);
		}
	}

	public void rename(String nameKo, String nameEn) {
		this.nameKo = requireNonBlank(nameKo, "nameKo");
		this.nameEn = requireNonBlank(nameEn, "nameEn");
		validateNameLength(this.nameKo);
		validateNameLength(this.nameEn);
	}

	public void activate() {
		this.status = CompanyStatus.ACTIVE;
	}

	public void deactivate() {
		this.status = CompanyStatus.INACTIVE;
	}
}

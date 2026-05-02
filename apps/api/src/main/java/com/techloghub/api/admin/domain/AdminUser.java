package com.techloghub.api.admin.domain;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "admin_user",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_admin_user_email", columnNames = "email")
	}
)
public class AdminUser extends BaseEntity {

	private static final int EMAIL_MAX_LENGTH = 320;
	private static final int PASSWORD_HASH_MAX_LENGTH = 255;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "email", nullable = false, length = 320)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 30)
	private AdminRole role;

	@Column(name = "active", nullable = false)
	private boolean active;

	private AdminUser(String email, String passwordHash, AdminRole role) {
		this.email = requireNonBlank(email, "email").toLowerCase();
		this.passwordHash = requireNonBlank(passwordHash, "passwordHash");
		this.role = requireNonNull(role, "role");
		this.active = true;
		validateEmailLength(this.email);
		validatePasswordHashLength(this.passwordHash);
	}

	public static AdminUser create(String email, String passwordHash) {
		return new AdminUser(email, passwordHash, AdminRole.ADMIN);
	}

	public void changePasswordHash(String passwordHash) {
		this.passwordHash = requireNonBlank(passwordHash, "passwordHash");
		validatePasswordHashLength(this.passwordHash);
	}

	public void activate() {
		this.active = true;
	}

	public void deactivate() {
		this.active = false;
	}

	public void promote(AdminRole role) {
		this.role = requireNonNull(role, "role");
	}

	private static void validateEmailLength(String value) {
		if (value.length() > EMAIL_MAX_LENGTH) {
			throw new IllegalArgumentException("email length must be <= " + EMAIL_MAX_LENGTH);
		}
	}

	private static void validatePasswordHashLength(String value) {
		if (value.length() > PASSWORD_HASH_MAX_LENGTH) {
			throw new IllegalArgumentException("passwordHash length must be <= " + PASSWORD_HASH_MAX_LENGTH);
		}
	}
}

package com.techloghub.api.admin.domain;

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
	name = "admin_user",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_admin_user_email", columnNames = "email")
	}
)
public class AdminUser extends BaseEntity {

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

	protected AdminUser() {
	}

	private AdminUser(String email, String passwordHash, AdminRole role) {
		this.email = requireNonBlank(email, "email").toLowerCase();
		this.passwordHash = requireNonBlank(passwordHash, "passwordHash");
		this.role = requireNonNull(role, "role");
		this.active = true;
	}

	public static AdminUser create(String email, String passwordHash) {
		return new AdminUser(email, passwordHash, AdminRole.ADMIN);
	}

	public void changePasswordHash(String passwordHash) {
		this.passwordHash = requireNonBlank(passwordHash, "passwordHash");
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

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public AdminRole getRole() {
		return role;
	}

	public boolean isActive() {
		return active;
	}
}

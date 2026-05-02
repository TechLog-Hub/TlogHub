package com.techloghub.api.admin.domain;

import static com.techloghub.api.common.domain.DomainGuard.normalizeBlankToNull;
import static com.techloghub.api.common.domain.DomainGuard.requireNonBlank;
import static com.techloghub.api.common.domain.DomainGuard.requireNonNull;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(
	name = "admin_audit_log",
	indexes = {
		@Index(name = "idx_admin_audit_log_admin_user_id", columnList = "admin_user_id"),
		@Index(name = "idx_admin_audit_log_target", columnList = "target_type, target_id")
	}
)
public class AdminAuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_user_id")
	private AdminUser adminUser;

	@Column(name = "action_type", nullable = false, length = 100)
	private String actionType;

	@Column(name = "target_type", nullable = false, length = 100)
	private String targetType;

	@Column(name = "target_id", nullable = false)
	private Long targetId;

	@Column(name = "before_json", columnDefinition = "text")
	private String beforeJson;

	@Column(name = "after_json", columnDefinition = "text")
	private String afterJson;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected AdminAuditLog() {
	}

	private AdminAuditLog(
		AdminUser adminUser,
		String actionType,
		String targetType,
		Long targetId,
		String beforeJson,
		String afterJson,
		Instant createdAt
	) {
		this.adminUser = adminUser;
		this.actionType = requireNonBlank(actionType, "actionType");
		this.targetType = requireNonBlank(targetType, "targetType");
		this.targetId = requireNonNull(targetId, "targetId");
		this.beforeJson = normalizeBlankToNull(beforeJson);
		this.afterJson = normalizeBlankToNull(afterJson);
		this.createdAt = requireNonNull(createdAt, "createdAt");
	}

	public static AdminAuditLog record(
		AdminUser adminUser,
		String actionType,
		String targetType,
		Long targetId,
		String beforeJson,
		String afterJson,
		Instant createdAt
	) {
		return new AdminAuditLog(adminUser, actionType, targetType, targetId, beforeJson, afterJson, createdAt);
	}

	public Long getId() {
		return id;
	}

	public AdminUser getAdminUser() {
		return adminUser;
	}

	public String getActionType() {
		return actionType;
	}

	public String getTargetType() {
		return targetType;
	}

	public Long getTargetId() {
		return targetId;
	}

	public String getBeforeJson() {
		return beforeJson;
	}

	public String getAfterJson() {
		return afterJson;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}

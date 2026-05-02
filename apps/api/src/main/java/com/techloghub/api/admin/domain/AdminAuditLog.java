package com.techloghub.api.admin.domain;

import static com.techloghub.api.common.domain.DomainGuard.normalizeBlankToNull;
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "admin_audit_log",
	indexes = {
		@Index(name = "idx_admin_audit_log_admin_user_id", columnList = "admin_user_id"),
		@Index(name = "idx_admin_audit_log_target_created", columnList = "target_type, target_id, created_at")
	}
)
public class AdminAuditLog extends BaseEntity {

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

	private AdminAuditLog(
		AdminUser adminUser,
		String actionType,
		String targetType,
		Long targetId,
		String beforeJson,
		String afterJson
	) {
		this.adminUser = adminUser;
		this.actionType = requireNonBlank(actionType, "actionType");
		this.targetType = requireNonBlank(targetType, "targetType");
		this.targetId = requireNonNull(targetId, "targetId");
		this.beforeJson = normalizeBlankToNull(beforeJson);
		this.afterJson = normalizeBlankToNull(afterJson);
	}

	public static AdminAuditLog record(
		AdminUser adminUser,
		String actionType,
		String targetType,
		Long targetId,
		String beforeJson,
		String afterJson
	) {
		return new AdminAuditLog(adminUser, actionType, targetType, targetId, beforeJson, afterJson);
	}
}

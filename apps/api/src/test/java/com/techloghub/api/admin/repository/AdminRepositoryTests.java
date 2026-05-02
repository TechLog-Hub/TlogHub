package com.techloghub.api.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.techloghub.api.admin.domain.AdminAuditLog;
import com.techloghub.api.admin.domain.AdminUser;
import com.techloghub.api.common.config.JpaAuditingConfiguration;
import com.techloghub.api.common.config.QueryDslConfiguration;

@ActiveProfiles("test")
@DataJpaTest
@Import({JpaAuditingConfiguration.class, QueryDslConfiguration.class})
class AdminRepositoryTests {

	@Autowired
	private AdminUserRepository adminUserRepository;

	@Autowired
	private AdminAuditLogRepository adminAuditLogRepository;

	@Test
	void findsAdminUserAndAuditLog() {
		AdminUser adminUser = adminUserRepository.saveAndFlush(AdminUser.create("ADMIN@TECHLOGHUB.LOCAL", "{noop}password"));
		AdminAuditLog auditLog = adminAuditLogRepository.saveAndFlush(AdminAuditLog.record(
			adminUser,
			"POST_PUBLISH",
			"archived_post",
			1L,
			"{}",
			"{\"status\":\"PUBLISHED\"}"
		));

		assertThat(adminUserRepository.findByEmail("admin@techloghub.local")).isPresent();
		assertThat(adminUserRepository.existsByEmail("admin@techloghub.local")).isTrue();
		assertThat(adminAuditLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
			"archived_post",
			1L,
			PageRequest.of(0, 10)
		)).containsExactly(auditLog);
		assertThat(adminAuditLogRepository.findByAdminUser_IdOrderByCreatedAtDesc(
			adminUser.getId(),
			PageRequest.of(0, 10)
		)).containsExactly(auditLog);
	}
}

package com.techloghub.api.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.techloghub.api.admin.domain.AdminAuditLog;

/**
 * 관리자 감사 로그의 영속성 조회 계약이다.
 */
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

	/**
	 * 대상 리소스 기준 감사 로그를 최신순으로 조회한다.
	 *
	 * @param targetType 대상 타입
	 * @param targetId 대상 ID
	 * @param pageable 페이지 요청
	 * @return 감사 로그 페이지
	 */
	Page<AdminAuditLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
		String targetType,
		Long targetId,
		Pageable pageable
	);

	/**
	 * 관리자 기준 감사 로그를 최신순으로 조회한다.
	 *
	 * @param adminUserId 관리자 ID
	 * @param pageable 페이지 요청
	 * @return 감사 로그 페이지
	 */
	Page<AdminAuditLog> findByAdminUser_IdOrderByCreatedAtDesc(Long adminUserId, Pageable pageable);
}

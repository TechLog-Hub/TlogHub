package com.techloghub.api.admin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techloghub.api.admin.domain.AdminUser;

/**
 * 관리자 계정의 영속성 조회 계약이다.
 */
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

	/**
	 * 이메일로 관리자 계정을 조회한다.
	 *
	 * @param email 이메일
	 * @return 조회된 관리자 계정
	 */
	Optional<AdminUser> findByEmail(String email);

	/**
	 * 이메일 중복 여부를 확인한다.
	 *
	 * @param email 이메일
	 * @return 존재하면 true
	 */
	boolean existsByEmail(String email);
}

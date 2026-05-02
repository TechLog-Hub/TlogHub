package com.techloghub.api.subscription.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techloghub.api.subscription.domain.Subscriber;

/**
 * 구독자의 영속성 조회 계약이다.
 */
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

	/**
	 * 이메일로 구독자를 조회한다.
	 *
	 * @param email 이메일
	 * @return 조회된 구독자
	 */
	Optional<Subscriber> findByEmail(String email);

	/**
	 * 이메일 중복 여부를 확인한다.
	 *
	 * @param email 이메일
	 * @return 존재하면 true
	 */
	boolean existsByEmail(String email);

	/**
	 * 관리 토큰 hash로 구독자를 조회한다.
	 *
	 * @param manageTokenHash 관리 토큰 hash
	 * @return 조회된 구독자
	 */
	Optional<Subscriber> findByManageTokenHash(String manageTokenHash);
}

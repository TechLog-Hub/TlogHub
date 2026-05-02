package com.techloghub.api.subscription.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techloghub.api.subscription.domain.SubscriptionVerificationRequest;

/**
 * 구독 확인 요청의 영속성 조회 계약이다.
 */
public interface SubscriptionVerificationRequestRepository extends JpaRepository<SubscriptionVerificationRequest, Long> {

	/**
	 * 확인 토큰 hash로 요청을 조회한다.
	 *
	 * @param tokenHash 확인 토큰 hash
	 * @return 조회된 확인 요청
	 */
	Optional<SubscriptionVerificationRequest> findByTokenHash(String tokenHash);

	/**
	 * 구독자의 미사용 확인 요청을 조회한다.
	 *
	 * @param subscriberId 구독자 ID
	 * @return 미사용 확인 요청 목록
	 */
	List<SubscriptionVerificationRequest> findBySubscriber_IdAndUsedAtIsNull(Long subscriberId);
}

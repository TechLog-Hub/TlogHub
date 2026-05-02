package com.techloghub.api.subscription.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.techloghub.api.subscription.domain.CompanySubscription;

/**
 * 기업 구독 관계의 영속성 조회 계약이다.
 */
public interface CompanySubscriptionRepository extends JpaRepository<CompanySubscription, Long> {

	/**
	 * 구독자와 기업 slug로 기업 구독 관계를 조회한다.
	 *
	 * @param subscriberId 구독자 ID
	 * @param companySlug 기업 slug
	 * @return 조회된 기업 구독 관계
	 */
	Optional<CompanySubscription> findBySubscriber_IdAndCompany_Slug(Long subscriberId, String companySlug);

	/**
	 * 구독자의 활성 기업 구독 목록을 기업과 함께 조회한다.
	 *
	 * @param subscriberId 구독자 ID
	 * @return 활성 기업 구독 목록
	 */
	@EntityGraph(attributePaths = "company")
	List<CompanySubscription> findBySubscriber_IdAndActiveTrue(Long subscriberId);

	/**
	 * 활성 기업 구독 중복 여부를 확인한다.
	 *
	 * @param subscriberId 구독자 ID
	 * @param companyId 기업 ID
	 * @return 존재하면 true
	 */
	boolean existsBySubscriber_IdAndCompany_IdAndActiveTrue(Long subscriberId, Long companyId);
}

package com.techloghub.api.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techloghub.api.content.domain.Company;
import com.techloghub.api.content.domain.CompanyStatus;

/**
 * 기업 도메인의 영속성 조회 계약이다.
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {

	/**
	 * slug로 기업을 조회한다.
	 *
	 * @param slug 기업 slug
	 * @return 조회된 기업
	 */
	Optional<Company> findBySlug(String slug);

	/**
	 * slug 중복 여부를 확인한다.
	 *
	 * @param slug 기업 slug
	 * @return 존재하면 true
	 */
	boolean existsBySlug(String slug);

	/**
	 * 상태별 기업 목록을 한국어 이름순으로 조회한다.
	 *
	 * @param status 기업 상태
	 * @return 기업 목록
	 */
	List<Company> findByStatusOrderByNameKoAsc(CompanyStatus status);

	/**
	 * slug 목록과 상태로 기업 목록을 조회한다.
	 *
	 * @param slugs 기업 slug 목록
	 * @param status 기업 상태
	 * @return 기업 목록
	 */
	List<Company> findBySlugInAndStatus(List<String> slugs, CompanyStatus status);
}

package com.techloghub.api.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techloghub.api.content.domain.JobCategory;

/**
 * 직군 카테고리의 영속성 조회 계약이다.
 */
public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {

	/**
	 * 코드로 직군을 조회한다.
	 *
	 * @param code 직군 코드
	 * @return 조회된 직군
	 */
	Optional<JobCategory> findByCode(String code);

	/**
	 * 활성 직군 목록을 노출 순서대로 조회한다.
	 *
	 * @return 활성 직군 목록
	 */
	List<JobCategory> findByActiveTrueOrderByDisplayOrderAsc();
}

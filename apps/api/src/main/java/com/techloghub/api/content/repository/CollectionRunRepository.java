package com.techloghub.api.content.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.techloghub.api.content.domain.CollectionRun;
import com.techloghub.api.content.domain.CollectionRunStatus;

/**
 * 수집 실행 이력의 영속성 조회 계약이다.
 */
public interface CollectionRunRepository extends JpaRepository<CollectionRun, Long> {

	/**
	 * 소스별 최근 수집 실행 이력을 조회한다.
	 *
	 * @param sourceBlogId 소스 ID
	 * @return 최근 수집 실행 이력
	 */
	List<CollectionRun> findTop20BySourceBlog_IdOrderByStartedAtDesc(Long sourceBlogId);

	/**
	 * 상태별 수집 실행 이력을 오래된 시작 시각순으로 조회한다.
	 *
	 * @param status 수집 실행 상태
	 * @return 수집 실행 이력
	 */
	List<CollectionRun> findByStatusOrderByStartedAtAsc(CollectionRunStatus status);

	/**
	 * 상태별 수집 실행 이력을 최신 시작 시각순으로 제한 조회한다.
	 *
	 * @param status 수집 실행 상태
	 * @param pageable 제한 조건
	 * @return 수집 실행 이력
	 */
	List<CollectionRun> findByStatusOrderByStartedAtDesc(CollectionRunStatus status, Pageable pageable);
}

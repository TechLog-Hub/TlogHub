package com.techloghub.api.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techloghub.api.content.domain.AiSummary;

/**
 * AI 요약의 영속성 조회 계약이다.
 */
public interface AiSummaryRepository extends JpaRepository<AiSummary, Long> {

	/**
	 * 글의 현재 요약을 조회한다.
	 *
	 * @param archivedPostId 아카이브 글 ID
	 * @return 현재 요약
	 */
	Optional<AiSummary> findByArchivedPost_IdAndCurrentTrue(Long archivedPostId);

	/**
	 * 글의 요약 이력을 최신 버전순으로 조회한다.
	 *
	 * @param archivedPostId 아카이브 글 ID
	 * @return 요약 이력
	 */
	List<AiSummary> findByArchivedPost_IdOrderBySummaryVersionDesc(Long archivedPostId);
}

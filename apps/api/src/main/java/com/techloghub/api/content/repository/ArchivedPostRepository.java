package com.techloghub.api.content.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.techloghub.api.content.domain.ArchivedPost;
import com.techloghub.api.content.domain.VisibilityState;

/**
 * 아카이브 글의 공개 조회, 관리자 조회, 수집 중복 검사를 위한 영속성 조회 계약이다.
 */
public interface ArchivedPostRepository extends JpaRepository<ArchivedPost, Long> {

	/**
	 * 공개 상태를 함께 확인하며 slug로 글을 조회한다.
	 *
	 * @param slug 글 slug
	 * @param visibilityState 공개 상태
	 * @return 조회된 글
	 */
	Optional<ArchivedPost> findBySlugAndVisibilityState(String slug, VisibilityState visibilityState);

	/**
	 * 상세 화면에 필요한 연관 정보를 함께 조회한다.
	 *
	 * @param slug 글 slug
	 * @param visibilityState 공개 상태
	 * @return 상세 조회용 글
	 */
	@EntityGraph(attributePaths = {"company", "sourceBlog", "jobCategories", "topicTags"})
	Optional<ArchivedPost> findDetailedBySlugAndVisibilityState(String slug, VisibilityState visibilityState);

	/**
	 * canonical fingerprint로 글을 조회한다.
	 *
	 * @param canonicalFingerprint 정규화 fingerprint
	 * @return 조회된 글
	 */
	Optional<ArchivedPost> findByCanonicalFingerprint(String canonicalFingerprint);

	/**
	 * canonical fingerprint 중복 여부를 확인한다.
	 *
	 * @param canonicalFingerprint 정규화 fingerprint
	 * @return 존재하면 true
	 */
	boolean existsByCanonicalFingerprint(String canonicalFingerprint);

	/**
	 * 공개 상태별 최신 글 목록을 조회한다.
	 *
	 * @param visibilityState 공개 상태
	 * @param pageable 페이지 요청
	 * @return 글 페이지
	 */
	Page<ArchivedPost> findByVisibilityStateOrderByPublishedAtDesc(
		VisibilityState visibilityState,
		Pageable pageable
	);

	/**
	 * 기업 slug와 공개 상태 기준으로 최신 글 목록을 조회한다.
	 *
	 * @param companySlug 기업 slug
	 * @param visibilityState 공개 상태
	 * @param pageable 페이지 요청
	 * @return 글 페이지
	 */
	Page<ArchivedPost> findByCompany_SlugAndVisibilityStateOrderByPublishedAtDesc(
		String companySlug,
		VisibilityState visibilityState,
		Pageable pageable
	);

	/**
	 * 직군 코드와 공개 상태 기준으로 최신 글 목록을 조회한다.
	 *
	 * @param jobCode 직군 코드
	 * @param visibilityState 공개 상태
	 * @param pageable 페이지 요청
	 * @return 글 페이지
	 */
	Page<ArchivedPost> findDistinctByJobCategories_CodeAndVisibilityStateOrderByPublishedAtDesc(
		String jobCode,
		VisibilityState visibilityState,
		Pageable pageable
	);

	/**
	 * 태그 slug와 공개 상태 기준으로 최신 글 목록을 조회한다.
	 *
	 * @param tagSlug 태그 slug
	 * @param visibilityState 공개 상태
	 * @param pageable 페이지 요청
	 * @return 글 페이지
	 */
	Page<ArchivedPost> findDistinctByTopicTags_SlugAndVisibilityStateOrderByPublishedAtDesc(
		String tagSlug,
		VisibilityState visibilityState,
		Pageable pageable
	);
}

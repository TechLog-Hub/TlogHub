package com.techloghub.api.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.techloghub.api.content.domain.SourceBlog;
import com.techloghub.api.content.domain.SourceBlogStatus;

/**
 * 기술 블로그 소스의 영속성 조회 계약이다.
 */
public interface SourceBlogRepository extends JpaRepository<SourceBlog, Long>, AdminSourceBlogQueryRepository {

	/**
	 * feed URL로 소스를 조회한다.
	 *
	 * @param feedUrl RSS/Atom feed URL
	 * @return 조회된 소스
	 */
	Optional<SourceBlog> findByFeedUrl(String feedUrl);

	/**
	 * feed URL 중복 여부를 확인한다.
	 *
	 * @param feedUrl RSS/Atom feed URL
	 * @return 존재하면 true
	 */
	boolean existsByFeedUrl(String feedUrl);

	/**
	 * 상태별 소스 목록을 기업과 함께 조회한다.
	 *
	 * @param status 소스 상태
	 * @return 소스 목록
	 */
	@EntityGraph(attributePaths = "company")
	List<SourceBlog> findByStatusOrderByIdAsc(SourceBlogStatus status);

	/**
	 * 기업 slug 기준으로 소스 목록을 조회한다.
	 *
	 * @param companySlug 기업 slug
	 * @return 소스 목록
	 */
	List<SourceBlog> findByCompany_SlugOrderByIdAsc(String companySlug);
}

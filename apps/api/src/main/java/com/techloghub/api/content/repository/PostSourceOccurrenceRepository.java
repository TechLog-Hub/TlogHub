package com.techloghub.api.content.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techloghub.api.content.domain.PostSourceOccurrence;

/**
 * 수집 글 발생 이력의 영속성 조회 계약이다.
 */
public interface PostSourceOccurrenceRepository extends JpaRepository<PostSourceOccurrence, Long> {

	/**
	 * 같은 소스에서 같은 원문 URL이 이미 수집됐는지 확인한다.
	 *
	 * @param sourceBlogId 소스 ID
	 * @param originUrl 원문 URL
	 * @return 존재하면 true
	 */
	boolean existsBySourceBlog_IdAndOriginUrl(Long sourceBlogId, String originUrl);

	/**
	 * 같은 소스에서 지정한 원문 URL 목록에 해당하는 발생 이력을 조회한다.
	 *
	 * @param sourceBlogId 소스 ID
	 * @param originUrls 원문 URL 목록
	 * @return 발생 이력 목록
	 */
	List<PostSourceOccurrence> findBySourceBlog_IdAndOriginUrlIn(Long sourceBlogId, Collection<String> originUrls);

	/**
	 * 대표 글에 연결된 발생 이력을 최신 발행일순으로 조회한다.
	 *
	 * @param archivedPostId 아카이브 글 ID
	 * @return 발생 이력 목록
	 */
	List<PostSourceOccurrence> findByArchivedPost_IdOrderByPublishedAtDesc(Long archivedPostId);
}

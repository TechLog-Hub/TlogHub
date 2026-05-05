package com.techloghub.api.content.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techloghub.api.content.domain.FeedEntrySnapshot;

/**
 * feed entry 원천 snapshot의 영속성 조회 계약이다.
 */
public interface FeedEntrySnapshotRepository extends JpaRepository<FeedEntrySnapshot, Long> {

	/**
	 * 같은 소스에서 지정한 원문 URL 목록에 해당하는 snapshot을 조회한다.
	 *
	 * @param sourceBlogId 소스 ID
	 * @param originUrls 원문 URL 목록
	 * @return snapshot 목록
	 */
	List<FeedEntrySnapshot> findBySourceBlog_IdAndOriginUrlIn(Long sourceBlogId, Collection<String> originUrls);

	/**
	 * 대표 글에 연결된 snapshot을 최신 수집순으로 조회한다.
	 *
	 * @param archivedPostId 아카이브 글 ID
	 * @return snapshot 목록
	 */
	List<FeedEntrySnapshot> findByArchivedPost_IdOrderByCollectedAtDesc(Long archivedPostId);
}

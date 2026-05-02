package com.techloghub.api.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techloghub.api.content.domain.TopicTag;

/**
 * 주제 태그의 영속성 조회 계약이다.
 */
public interface TopicTagRepository extends JpaRepository<TopicTag, Long> {

	/**
	 * slug로 태그를 조회한다.
	 *
	 * @param slug 태그 slug
	 * @return 조회된 태그
	 */
	Optional<TopicTag> findBySlug(String slug);

	/**
	 * 활성 태그 목록을 라벨순으로 조회한다.
	 *
	 * @return 활성 태그 목록
	 */
	List<TopicTag> findByActiveTrueOrderByLabelAsc();
}

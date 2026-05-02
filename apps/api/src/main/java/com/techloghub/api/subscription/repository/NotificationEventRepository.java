package com.techloghub.api.subscription.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techloghub.api.subscription.domain.NotificationEvent;
import com.techloghub.api.subscription.domain.NotificationStatus;

/**
 * 알림 이벤트의 영속성 조회 계약이다.
 */
public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {

	/**
	 * 구독자와 글 기준으로 알림 이벤트 중복 여부를 확인한다.
	 *
	 * @param subscriberId 구독자 ID
	 * @param archivedPostId 아카이브 글 ID
	 * @return 존재하면 true
	 */
	boolean existsBySubscriber_IdAndArchivedPost_Id(Long subscriberId, Long archivedPostId);

	/**
	 * 발송 대기 이벤트를 오래된 요청 순서로 조회한다.
	 *
	 * @param status 알림 상태
	 * @return 발송 처리 대상 목록
	 */
	List<NotificationEvent> findTop100ByStatusOrderByRequestedAtAsc(NotificationStatus status);
}

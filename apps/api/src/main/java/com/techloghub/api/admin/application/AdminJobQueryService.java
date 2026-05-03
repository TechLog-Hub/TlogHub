package com.techloghub.api.admin.application;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techloghub.api.admin.dto.AdminFailureResponse;
import com.techloghub.api.content.domain.CollectionRunStatus;
import com.techloghub.api.content.repository.CollectionRunRepository;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 Batch job 조회 use-case다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminJobQueryService {

	private final CollectionRunRepository collectionRunRepository;

	public List<AdminFailureResponse> getRecentCollectionFailures(int size) {
		return collectionRunRepository.findByStatusOrderByStartedAtDesc(
				CollectionRunStatus.FAILED,
				PageRequest.of(0, size)
			)
			.stream()
			.map(AdminFailureResponse::from)
			.toList();
	}
}

package com.techloghub.api.common.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import com.techloghub.api.common.util.CollectionSupport;

/**
 * offset 기반 목록 API의 공통 응답이다.
 *
 * @param content 현재 페이지 목록
 * @param page 0부터 시작하는 페이지 번호
 * @param size 페이지 크기
 * @param totalElements 전체 항목 수
 * @param totalPages 전체 페이지 수
 * @param <T> 항목 타입
 */
public record PageResponse<T>(
	List<T> content,
	int page,
	int size,
	long totalElements,
	int totalPages
) {
	public static <T> PageResponse<T> from(Page<T> page) {
		return new PageResponse<>(
			CollectionSupport.nullToEmptyList(page.getContent()),
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages()
		);
	}

	public static <T> PageResponse<T> of(List<T> content, Page<?> page) {
		return new PageResponse<>(
			CollectionSupport.nullToEmptyList(content),
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages()
		);
	}
}

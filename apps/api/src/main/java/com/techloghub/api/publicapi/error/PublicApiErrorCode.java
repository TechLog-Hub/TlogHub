package com.techloghub.api.publicapi.error;

import org.springframework.http.HttpStatus;

import com.techloghub.api.common.error.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 공개 API에서 클라이언트가 분기할 수 있는 도메인 오류 코드다.
 */
@RequiredArgsConstructor
public enum PublicApiErrorCode implements ErrorCode {
	PUBLIC_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "PUBLIC_POST_NOT_FOUND", "공개 글을 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	@Override
	public HttpStatus httpStatus() {
		return httpStatus;
	}

	@Override
	public String code() {
		return code;
	}

	@Override
	public String message() {
		return message;
	}
}

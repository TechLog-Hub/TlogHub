package com.techloghub.api.common.error;

import org.springframework.http.HttpStatus;

/**
 * API error response의 machine-readable code와 HTTP 상태를 정의한다.
 */
public interface ErrorCode {

	HttpStatus httpStatus();

	String code();

	String message();
}

package com.techloghub.api.common.error;

import java.util.List;

/**
 * 공통 error response 생성 책임을 한 곳에 모은다.
 */
public class ErrorResponseFactory {

	private final TraceIdResolver traceIdResolver;

	public ErrorResponseFactory() {
		this(new TraceIdResolver());
	}

	public ErrorResponseFactory(TraceIdResolver traceIdResolver) {
		this.traceIdResolver = traceIdResolver;
	}

	public ErrorResponse from(ErrorCode errorCode) {
		return ErrorResponse.of(errorCode, errorCode.message(), traceIdResolver.resolve());
	}

	public ErrorResponse from(ErrorCode errorCode, String safeMessage) {
		return ErrorResponse.of(errorCode, safeMessage, traceIdResolver.resolve());
	}

	public ErrorResponse from(ErrorCode errorCode, List<FieldErrorResponse> errors) {
		return ErrorResponse.of(errorCode, errors, traceIdResolver.resolve());
	}
}

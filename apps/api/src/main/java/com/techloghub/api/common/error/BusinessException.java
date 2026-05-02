package com.techloghub.api.common.error;

import lombok.Getter;

/**
 * 예상 가능한 application/domain 오류를 API error code와 함께 전달한다.
 */
@Getter
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;
	private final String safeMessage;

	public BusinessException(ErrorCode errorCode) {
		this(errorCode, errorCode.message(), null);
	}

	public BusinessException(ErrorCode errorCode, String message) {
		this(errorCode, message, null);
	}

	public BusinessException(ErrorCode errorCode, Throwable cause) {
		this(errorCode, errorCode.message(), cause);
	}

	public BusinessException(ErrorCode errorCode, String safeMessage, Throwable cause) {
		super(normalizeSafeMessage(errorCode, safeMessage), cause);
		this.errorCode = errorCode;
		this.safeMessage = normalizeSafeMessage(errorCode, safeMessage);
	}

	private static String normalizeSafeMessage(ErrorCode errorCode, String safeMessage) {
		if (safeMessage == null || safeMessage.isBlank()) {
			return errorCode.message();
		}
		return safeMessage;
	}
}

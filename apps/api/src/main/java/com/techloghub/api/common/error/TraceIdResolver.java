package com.techloghub.api.common.error;

import org.slf4j.MDC;

/**
 * error response에 연결할 추적 식별자를 MDC에서 조회한다.
 */
public class TraceIdResolver {

	public String resolve() {
		String traceId = MDC.get("traceId");
		if (traceId != null && !traceId.isBlank()) {
			return traceId;
		}
		String requestId = MDC.get("requestId");
		if (requestId != null && !requestId.isBlank()) {
			return requestId;
		}
		return null;
	}
}

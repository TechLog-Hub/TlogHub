package com.techloghub.api.common.error;

import org.slf4j.MDC;

import com.techloghub.api.common.util.StringNormalizer;

/**
 * error response에 연결할 추적 식별자를 MDC에서 조회한다.
 */
public class TraceIdResolver {

	public String resolve() {
		String traceId = MDC.get("traceId");
		String normalizedTraceId = StringNormalizer.trimToNull(traceId);
		if (normalizedTraceId != null) {
			return normalizedTraceId;
		}
		String requestId = MDC.get("requestId");
		String normalizedRequestId = StringNormalizer.trimToNull(requestId);
		if (normalizedRequestId != null) {
			return normalizedRequestId;
		}
		return null;
	}
}

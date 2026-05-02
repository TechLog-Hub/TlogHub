package com.techloghub.api.common.util;

import java.util.Locale;

/**
 * cache key, idempotency key 등 반복되는 문자열 key 생성을 표준화한다.
 */
public final class KeySupport {

	private KeySupport() {
	}

	public static String cacheKeyOf(String prefix, Object... parts) {
		StringBuilder builder = new StringBuilder(normalizeCachePart(prefix));
		if (parts == null) {
			return builder.toString();
		}
		for (Object part : parts) {
			builder.append(':').append(normalizeCachePart(part));
		}
		return builder.toString();
	}

	public static String normalizeCachePart(Object value) {
		if (value == null) {
			return "null";
		}
		return String.valueOf(value)
			.trim()
			.toLowerCase(Locale.ROOT)
			.replace(' ', '-');
	}
}

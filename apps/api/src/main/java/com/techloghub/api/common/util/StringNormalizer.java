package com.techloghub.api.common.util;

import java.util.regex.Pattern;

/**
 * 요청/응답 문자열 정규화에서 반복되는 null, blank, whitespace 처리를 모은다.
 */
public final class StringNormalizer {

	private static final Pattern WHITESPACE = Pattern.compile("\\s+");

	private StringNormalizer() {
	}

	public static String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	public static String trimToEmpty(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		return value.trim();
	}

	public static String normalizeWhitespaceToNull(String value) {
		String trimmed = trimToNull(value);
		if (trimmed == null) {
			return null;
		}
		return WHITESPACE.matcher(trimmed).replaceAll(" ");
	}

	public static String truncate(String value, int maxLength) {
		if (maxLength < 0) {
			throw new IllegalArgumentException("maxLength는 0 이상이어야 합니다.");
		}
		if (value == null || value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength);
	}
}

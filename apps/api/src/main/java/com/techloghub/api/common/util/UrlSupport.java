package com.techloghub.api.common.util;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * URL parsing과 정규화에서 반복되는 검증을 모은다.
 */
public final class UrlSupport {

	private UrlSupport() {
	}

	public static URI parseHttpUri(String value, String fieldName) {
		String normalized = StringNormalizer.trimToNull(value);
		if (normalized == null) {
			throw new IllegalArgumentException(fieldName + "은 비어 있을 수 없습니다.");
		}
		URI uri;
		try {
			uri = URI.create(normalized);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(fieldName + " 형식이 올바르지 않습니다.", exception);
		}
		String scheme = uri.getScheme();
		if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
			throw new IllegalArgumentException(fieldName + "은 http 또는 https URL이어야 합니다.");
		}
		if (uri.getHost() == null || uri.getHost().isBlank()) {
			throw new IllegalArgumentException(fieldName + "은 host를 포함해야 합니다.");
		}
		return uri;
	}

	public static String normalizeUrl(String value) {
		URI uri = parseHttpUri(value, "url");
		int port = uri.getPort();
		String portPart = port < 0 ? "" : ":" + port;
		String origin = uri.getScheme().toLowerCase(Locale.ROOT)
			+ "://"
			+ uri.getHost().toLowerCase(Locale.ROOT)
			+ portPart;
		String path = uri.getRawPath() == null ? "" : removeTrailingSlash(uri.getRawPath());
		String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
		return origin + path + query;
	}

	public static String extractHost(String value) {
		return parseHttpUri(value, "url").getHost().toLowerCase(Locale.ROOT);
	}

	public static String urlEncode(String value) {
		if (value == null) {
			return null;
		}
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	public static String urlDecode(String value) {
		if (value == null) {
			return null;
		}
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

	public static String removeTrailingSlash(String value) {
		if (value == null || value.length() <= 1 || !value.endsWith("/")) {
			return value;
		}
		int end = value.length();
		while (end > 1 && value.charAt(end - 1) == '/') {
			end--;
		}
		return value.substring(0, end);
	}
}

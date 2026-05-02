package com.techloghub.api.common.util;

/**
 * 응답과 로그에서 민감 값을 안전하게 축약한다.
 */
public final class MaskingSupport {

	private MaskingSupport() {
	}

	public static String maskEmail(String email) {
		String normalized = StringNormalizer.trimToNull(email);
		if (normalized == null) {
			return null;
		}
		int atIndex = normalized.indexOf('@');
		if (atIndex <= 0) {
			return "***";
		}
		String local = normalized.substring(0, atIndex);
		String domain = normalized.substring(atIndex + 1);
		String maskedLocal = local.length() <= 2
			? local.charAt(0) + "***"
			: local.substring(0, 2) + "***";
		return maskedLocal + "@" + domain;
	}

	public static String maskSecret(String value) {
		String normalized = StringNormalizer.trimToNull(value);
		if (normalized == null) {
			return null;
		}
		if (normalized.length() <= 8) {
			return "***";
		}
		return normalized.substring(0, 4) + "***" + normalized.substring(normalized.length() - 4);
	}
}

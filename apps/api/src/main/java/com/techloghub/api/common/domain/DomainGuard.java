package com.techloghub.api.common.domain;

import java.util.Collection;
import java.util.Objects;

import com.techloghub.api.common.util.StringNormalizer;

public final class DomainGuard {

	private DomainGuard() {
	}

	public static <T> T requireNonNull(T value, String fieldName) {
		return Objects.requireNonNull(value, fieldName + " must not be null");
	}

	public static String requireTrimmedNonBlank(String value, String fieldName) {
		return requireNonBlank(value, fieldName);
	}

	public static void requireBoolean(boolean condition, String message) {
		if (!condition) {
			throw new IllegalArgumentException(message);
		}
	}

	public static String requireNonBlank(String value, String fieldName) {
		String normalizedValue = StringNormalizer.trimToNull(value);
		if (normalizedValue == null) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}
		return normalizedValue;
	}

	public static String normalizeBlankToNull(String value) {
		return StringNormalizer.trimToNull(value);
	}

	public static <T> Collection<T> requireMaxSize(Collection<T> values, int maxSize, String fieldName) {
		requireNonNull(values, fieldName);
		if (values.size() > maxSize) {
			throw new IllegalArgumentException(fieldName + " must be less than or equal to " + maxSize);
		}
		return values;
	}

	public static int requireNonNegative(int value, String fieldName) {
		if (value < 0) {
			throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
		}
		return value;
	}
}

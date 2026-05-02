package com.techloghub.api.common.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * enum 이름/code parsing을 일관되게 처리한다.
 */
public final class EnumParser {

	private static final Map<Class<?>, Map<String, ? extends Enum<?>>> NAME_CACHE = new ConcurrentHashMap<>();

	private EnumParser() {
	}

	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> E findNameIgnoreCase(Class<E> enumType, String value) {
		String normalized = StringNormalizer.trimToNull(value);
		if (normalized == null) {
			return null;
		}
		Map<String, E> enumMap = (Map<String, E>)NAME_CACHE.computeIfAbsent(enumType, EnumParser::indexByUpperName);
		return enumMap.get(normalized.toUpperCase(Locale.ROOT));
	}

	public static <E extends Enum<E>> E parseNameIgnoreCase(Class<E> enumType, String value, String fieldName) {
		E result = findNameIgnoreCase(enumType, value);
		if (result == null) {
			throw new IllegalArgumentException(fieldName + " 값이 올바르지 않습니다. 허용값: " + allowedNames(enumType));
		}
		return result;
	}

	public static <E extends Enum<E>> String allowedNames(Class<E> enumType) {
		return String.join(
			", ",
			Arrays.stream(enumType.getEnumConstants())
				.map(Enum::name)
				.toList()
		);
	}

	private static Map<String, ? extends Enum<?>> indexByUpperName(Class<?> enumType) {
		Map<String, Enum<?>> result = new LinkedHashMap<>();
		for (Object value : enumType.getEnumConstants()) {
			Enum<?> enumValue = (Enum<?>)value;
			result.put(enumValue.name().toUpperCase(Locale.ROOT), enumValue);
		}
		return Map.copyOf(result);
	}
}

package com.techloghub.api.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * collection null 처리, 중복 제거, strict map 변환을 위한 공통 helper다.
 */
public final class CollectionSupport {

	private CollectionSupport() {
	}

	public static <T> List<T> nullToEmptyList(Collection<T> values) {
		if (values == null || values.isEmpty()) {
			return List.of();
		}
		return List.copyOf(values);
	}

	public static <T> boolean isNullOrEmpty(Collection<T> values) {
		return values == null || values.isEmpty();
	}

	public static <T, K> Predicate<T> distinctByKey(Function<T, K> keyExtractor) {
		Set<K> seen = ConcurrentHashMap.newKeySet();
		return value -> seen.add(keyExtractor.apply(value));
	}

	public static <T, K> Map<K, T> toMapStrict(Collection<T> values, Function<T, K> keyExtractor) {
		Map<K, T> result = new LinkedHashMap<>();
		if (values == null || values.isEmpty()) {
			return result;
		}
		for (T value : values) {
			K key = keyExtractor.apply(value);
			if (result.containsKey(key)) {
				throw new IllegalArgumentException("중복 key가 존재합니다. key=" + key);
			}
			result.put(key, value);
		}
		return result;
	}

	public static <T, K, V> Map<K, V> toValueMapStrict(
		Collection<T> values,
		Function<T, K> keyExtractor,
		Function<T, V> valueExtractor
	) {
		Map<K, V> result = new LinkedHashMap<>();
		if (values == null || values.isEmpty()) {
			return result;
		}
		for (T value : values) {
			K key = keyExtractor.apply(value);
			if (result.containsKey(key)) {
				throw new IllegalArgumentException("중복 key가 존재합니다. key=" + key);
			}
			result.put(key, valueExtractor.apply(value));
		}
		return result;
	}

	public static <T> List<List<T>> chunk(List<T> values, int chunkSize) {
		if (chunkSize <= 0) {
			throw new IllegalArgumentException("chunkSize는 1 이상이어야 합니다.");
		}
		if (values == null || values.isEmpty()) {
			return List.of();
		}
		List<List<T>> chunks = new ArrayList<>((values.size() + chunkSize - 1) / chunkSize);
		for (int start = 0; start < values.size(); start += chunkSize) {
			chunks.add(Collections.unmodifiableList(new ArrayList<>(
				values.subList(start, Math.min(start + chunkSize, values.size()))
			)));
		}
		return Collections.unmodifiableList(chunks);
	}
}

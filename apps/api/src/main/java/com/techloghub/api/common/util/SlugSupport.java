package com.techloghub.api.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * URL slug 생성과 검증을 담당한다.
 */
public final class SlugSupport {

	private static final Pattern NON_SLUG = Pattern.compile("[^a-z0-9]+");
	private static final Pattern MULTI_DASH = Pattern.compile("-+");
	private static final Pattern EDGE_DASH = Pattern.compile("^-|-$");
	private static final Pattern DIACRITICS = Pattern.compile("\\p{M}");
	private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

	private SlugSupport() {
	}

	public static String slugify(String value) {
		String normalized = StringNormalizer.trimToNull(value);
		if (normalized == null) {
			throw new IllegalArgumentException("slug 원본 값은 비어 있을 수 없습니다.");
		}
		String ascii = DIACRITICS.matcher(Normalizer.normalize(normalized, Normalizer.Form.NFD))
			.replaceAll("")
			.toLowerCase(Locale.ROOT)
			.trim();
		String slug = NON_SLUG.matcher(ascii).replaceAll("-");
		String result = EDGE_DASH.matcher(MULTI_DASH.matcher(slug).replaceAll("-")).replaceAll("");
		if (result.isEmpty()) {
			throw new IllegalArgumentException("slug 생성 결과가 비어 있습니다. 유효한 문자가 포함되어야 합니다.");
		}
		return result;
	}

	public static void validateSlug(String slug) {
		if (slug == null || !SLUG_PATTERN.matcher(slug).matches()) {
			throw new IllegalArgumentException("slug 형식이 올바르지 않습니다.");
		}
	}
}

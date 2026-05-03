package com.techloghub.api.worker.normalization;

import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.techloghub.api.common.util.SlugSupport;
import com.techloghub.api.content.domain.SourceBlog;

/**
 * 수집 글 slug를 source/company/title 기반으로 생성한다.
 */
@Component
public class PostSlugGenerator {

	private static final int SLUG_MAX_LENGTH = 180;
	private static final int BASE_MAX_LENGTH = 170;
	private static final int MAX_COLLISION_ATTEMPT = 1_000;

	public String generate(SourceBlog sourceBlog, String title, Predicate<String> existsBySlug) {
		String base = trimEdgeDash(SlugSupport.slugify(sourceBlog.getCompany().getSlug() + "-" + title));
		if (base.length() > BASE_MAX_LENGTH) {
			base = trimEdgeDash(base.substring(0, BASE_MAX_LENGTH));
		}
		if (!existsBySlug.test(base)) {
			return base;
		}
		for (int sequence = 2; sequence <= MAX_COLLISION_ATTEMPT; sequence++) {
			String suffix = "-" + sequence;
			String candidateBase = base.length() + suffix.length() > SLUG_MAX_LENGTH
				? trimEdgeDash(base.substring(0, SLUG_MAX_LENGTH - suffix.length()))
				: base;
			String candidate = candidateBase + suffix;
			if (!existsBySlug.test(candidate)) {
				return candidate;
			}
		}
		throw new IllegalStateException("slug collision exceeded: " + base);
	}

	private static String trimEdgeDash(String value) {
		String result = value;
		while (result.startsWith("-")) {
			result = result.substring(1);
		}
		while (result.endsWith("-")) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}
}

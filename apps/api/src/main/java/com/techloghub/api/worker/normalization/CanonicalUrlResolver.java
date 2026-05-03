package com.techloghub.api.worker.normalization;

import org.springframework.stereotype.Component;

import com.techloghub.api.common.util.StringNormalizer;
import com.techloghub.api.common.util.UrlSupport;

/**
 * feed entry의 canonical URL을 origin URL fallback 정책으로 정규화한다.
 */
@Component
public class CanonicalUrlResolver {

	public String resolve(String canonicalUrl, String originUrl) {
		String normalizedCanonical = StringNormalizer.trimToNull(canonicalUrl);
		if (normalizedCanonical != null) {
			try {
				return UrlSupport.normalizeUrl(normalizedCanonical);
			} catch (IllegalArgumentException ignored) {
				// canonical link 품질이 낮은 feed는 origin URL을 fallback으로 사용한다.
			}
		}
		return UrlSupport.normalizeUrl(originUrl);
	}
}

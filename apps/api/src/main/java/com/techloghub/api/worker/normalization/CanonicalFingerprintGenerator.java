package com.techloghub.api.worker.normalization;

import org.springframework.stereotype.Component;

import com.techloghub.api.common.util.CryptoSupport;
import com.techloghub.api.common.util.UrlSupport;

/**
 * canonical URL 기반 exact duplicate fingerprint를 생성한다.
 */
@Component
public class CanonicalFingerprintGenerator {

	public String generate(String canonicalUrl) {
		return CryptoSupport.sha256Hex(UrlSupport.normalizeUrl(canonicalUrl));
	}
}

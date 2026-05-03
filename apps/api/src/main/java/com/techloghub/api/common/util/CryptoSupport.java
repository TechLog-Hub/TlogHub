package com.techloghub.api.common.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * token hash, fingerprint, webhook signature 등에 쓰는 crypto helper다.
 */
public final class CryptoSupport {

	private CryptoSupport() {
	}

	public static String sha256Hex(String value) {
		if (value == null) {
			throw new IllegalArgumentException("hash 대상 값은 null일 수 없습니다.");
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 algorithm을 사용할 수 없습니다.", exception);
		}
	}

	public static String hmacSha256Hex(String secret, String value) {
		if (secret == null || value == null) {
			throw new IllegalArgumentException("HMAC secret과 value는 null일 수 없습니다.");
		}
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("HMAC-SHA256 생성에 실패했습니다.", exception);
		}
	}
}

package com.techloghub.api.admin.application;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

/**
 * 관리자 bearer session token 원문을 생성한다.
 */
@Component
public class AdminSessionTokenGenerator {

	private static final int TOKEN_BYTE_LENGTH = 32;

	private final SecureRandom secureRandom = new SecureRandom();

	public String generate() {
		byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
		secureRandom.nextBytes(tokenBytes);
		return Base64.getUrlEncoder()
			.withoutPadding()
			.encodeToString(tokenBytes);
	}
}

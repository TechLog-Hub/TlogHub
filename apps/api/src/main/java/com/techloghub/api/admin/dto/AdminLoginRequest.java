package com.techloghub.api.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 관리자 로그인 요청이다.
 */
public record AdminLoginRequest(
	@NotBlank(message = "email은 필수입니다.")
	@Email(message = "email 형식이 올바르지 않습니다.")
	@Size(max = 320, message = "email은 최대 320자까지 입력할 수 있습니다.")
	String email,

	@NotBlank(message = "password는 필수입니다.")
	@Size(max = 200, message = "password는 최대 200자까지 입력할 수 있습니다.")
	String password
) {
}

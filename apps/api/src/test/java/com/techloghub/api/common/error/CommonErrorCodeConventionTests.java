package com.techloghub.api.common.error;

import static com.techloghub.api.testsupport.TestTags.UNIT;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(UNIT)
class CommonErrorCodeConventionTests {

	private static final Pattern ERROR_CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

	@Test
	void commonErrorCodesFollowPublicContract() {
		List<CommonErrorCode> values = Arrays.asList(CommonErrorCode.values());

		assertThat(values)
			.extracting(CommonErrorCode::code)
			.doesNotHaveDuplicates()
			.allMatch(code -> ERROR_CODE_PATTERN.matcher(code).matches());

		assertThat(values)
			.allSatisfy(errorCode -> {
				assertThat(errorCode.httpStatus()).isNotNull();
				assertThat(errorCode.code()).isNotBlank();
				assertThat(errorCode.message()).isNotBlank();
				assertThat(errorCode.logLevel()).isNotNull();
			});
	}

	@Test
	void businessExceptionKeepsSafeMessageSeparateFromCause() {
		IllegalStateException cause = new IllegalStateException("database detail");

		BusinessException exception = new BusinessException(
			CommonErrorCode.CONFLICT,
			"현재 상태에서는 처리할 수 없습니다.",
			cause
		);

		assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.CONFLICT);
		assertThat(exception.getSafeMessage()).isEqualTo("현재 상태에서는 처리할 수 없습니다.");
		assertThat(exception.getCause()).isSameAs(cause);
	}
}

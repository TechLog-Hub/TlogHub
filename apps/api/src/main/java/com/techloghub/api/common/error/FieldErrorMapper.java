package com.techloghub.api.common.error;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.techloghub.api.common.util.StringNormalizer;

import jakarta.validation.ConstraintViolation;

/**
 * Spring validation/binding 오류를 공개 가능한 field error로 변환한다.
 */
public class FieldErrorMapper {

	private static final int MAX_REJECTED_VALUE_LENGTH = 80;
	private static final Set<String> SENSITIVE_FIELD_KEYWORDS = Set.of(
		"password",
		"token",
		"secret",
		"credential",
		"authorization",
		"cookie",
		"email",
		"phone"
	);

	public List<FieldErrorResponse> fromBindingResult(BindingResult bindingResult) {
		List<FieldErrorResponse> errors = new ArrayList<>();
		errors.addAll(bindingResult.getFieldErrors().stream()
			.map(this::fromFieldError)
			.toList());
		errors.addAll(bindingResult.getGlobalErrors().stream()
			.map(this::fromObjectError)
			.toList());
		return errors;
	}

	public List<FieldErrorResponse> fromConstraintViolations(Set<ConstraintViolation<?>> violations) {
		return violations.stream()
			.map(violation -> FieldErrorResponse.of(
				violation.getPropertyPath().toString(),
				safeReason(violation.getMessage()),
				safeRejectedValue(violation.getPropertyPath().toString(), violation.getInvalidValue())
			))
			.toList();
	}

	public FieldErrorResponse fromTypeMismatch(String field, Object rejectedValue, String requiredTypeName) {
		String reason = requiredTypeName == null
			? "요청 값의 타입이 올바르지 않습니다."
			: requiredTypeName + " 형식으로 입력해 주세요.";
		return FieldErrorResponse.of(field, reason, safeRejectedValue(field, rejectedValue));
	}

	public FieldErrorResponse fromMissingValue(String field) {
		return FieldErrorResponse.of(field, "필수 요청 값입니다.");
	}

	public FieldErrorResponse fromBodyNotReadable() {
		return FieldErrorResponse.of("body", "요청 본문을 읽을 수 없습니다.");
	}

	private FieldErrorResponse fromFieldError(FieldError error) {
		return FieldErrorResponse.of(
			error.getField(),
			safeReason(error.getDefaultMessage()),
			safeRejectedValue(error.getField(), error.getRejectedValue())
		);
	}

	private FieldErrorResponse fromObjectError(ObjectError error) {
		return FieldErrorResponse.of(error.getObjectName(), safeReason(error.getDefaultMessage()));
	}

	private String safeReason(String reason) {
		String normalizedReason = StringNormalizer.trimToNull(reason);
		if (normalizedReason == null) {
			return "요청 값이 올바르지 않습니다.";
		}
		return normalizedReason;
	}

	private String safeRejectedValue(String field, Object rejectedValue) {
		if (rejectedValue == null || isSensitiveField(field)) {
			return null;
		}
		String value = String.valueOf(rejectedValue);
		if (value.length() <= MAX_REJECTED_VALUE_LENGTH) {
			return value;
		}
		return StringNormalizer.truncate(value, MAX_REJECTED_VALUE_LENGTH) + "...";
	}

	private boolean isSensitiveField(String field) {
		if (field == null) {
			return false;
		}
		String normalized = field.toLowerCase(Locale.ROOT);
		for (String keyword : SENSITIVE_FIELD_KEYWORDS) {
			if (normalized.contains(keyword)) {
				return true;
			}
		}
		return false;
	}
}

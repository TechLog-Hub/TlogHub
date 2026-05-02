package com.techloghub.api.common.error;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * API 오류를 공통 error response contract로 변환한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		return ResponseEntity
			.status(errorCode.httpStatus())
			.body(ErrorResponse.of(errorCode, exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
		return invalidRequest(fieldErrors(exception.getBindingResult().getFieldErrors()));
	}

	@ExceptionHandler(BindException.class)
	ResponseEntity<ErrorResponse> handleBindException(BindException exception) {
		return invalidRequest(fieldErrors(exception.getBindingResult().getFieldErrors()));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException exception) {
		List<FieldErrorResponse> errors = exception.getConstraintViolations().stream()
			.map(violation -> new FieldErrorResponse(
				violation.getPropertyPath().toString(),
				violation.getMessage()
			))
			.toList();
		return invalidRequest(errors);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
		MissingServletRequestParameterException exception
	) {
		return invalidRequest(List.of(new FieldErrorResponse(exception.getParameterName(), "required parameter is missing")));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
		return invalidRequest(List.of(new FieldErrorResponse(exception.getName(), "request value type is invalid")));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ErrorResponse> handleException(Exception exception) {
		log.error("Unhandled API exception", exception);
		return ResponseEntity
			.status(CommonErrorCode.INTERNAL_ERROR.httpStatus())
			.body(ErrorResponse.of(CommonErrorCode.INTERNAL_ERROR));
	}

	private ResponseEntity<ErrorResponse> invalidRequest(List<FieldErrorResponse> errors) {
		return ResponseEntity
			.status(CommonErrorCode.INVALID_REQUEST.httpStatus())
			.body(ErrorResponse.of(CommonErrorCode.INVALID_REQUEST, errors));
	}

	private List<FieldErrorResponse> fieldErrors(List<org.springframework.validation.FieldError> fieldErrors) {
		return fieldErrors.stream()
			.map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
			.toList();
	}
}

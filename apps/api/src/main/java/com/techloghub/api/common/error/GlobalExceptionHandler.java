package com.techloghub.api.common.error;

import java.util.List;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
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

	private final FieldErrorMapper fieldErrorMapper = new FieldErrorMapper();
	private final ErrorResponseFactory errorResponseFactory = new ErrorResponseFactory();

	@ExceptionHandler(BusinessException.class)
	ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		logByLevel(errorCode, exception);
		return ResponseEntity
			.status(errorCode.httpStatus())
			.body(errorResponseFactory.from(errorCode, exception.getSafeMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
		return invalidRequest(fieldErrors(exception.getBindingResult()));
	}

	@ExceptionHandler(BindException.class)
	ResponseEntity<ErrorResponse> handleBindException(BindException exception) {
		return invalidRequest(fieldErrors(exception.getBindingResult()));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException exception) {
		List<FieldErrorResponse> errors = fieldErrorMapper.fromConstraintViolations(exception.getConstraintViolations());
		return invalidRequest(errors);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
		MissingServletRequestParameterException exception
	) {
		return invalidRequest(List.of(fieldErrorMapper.fromMissingValue(exception.getParameterName())));
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	ResponseEntity<ErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException exception) {
		return invalidRequest(List.of(fieldErrorMapper.fromMissingValue(exception.getHeaderName())));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
		String requiredType = exception.getRequiredType() == null ? null : exception.getRequiredType().getSimpleName();
		FieldErrorResponse error = fieldErrorMapper.fromTypeMismatch(
			exception.getName(),
			exception.getValue(),
			requiredType
		);
		return invalidRequest(List.of(error));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
		log.debug("Malformed request body", exception);
		return invalidRequest(List.of(fieldErrorMapper.fromBodyNotReadable()));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
		return ResponseEntity
			.status(CommonErrorCode.METHOD_NOT_ALLOWED.httpStatus())
			.body(errorResponseFactory.from(CommonErrorCode.METHOD_NOT_ALLOWED));
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException exception) {
		return ResponseEntity
			.status(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE.httpStatus())
			.body(errorResponseFactory.from(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ErrorResponse> handleException(Exception exception) {
		log.error("Unhandled API exception", exception);
		return ResponseEntity
			.status(CommonErrorCode.INTERNAL_ERROR.httpStatus())
			.body(errorResponseFactory.from(CommonErrorCode.INTERNAL_ERROR));
	}

	private ResponseEntity<ErrorResponse> invalidRequest(List<FieldErrorResponse> errors) {
		return ResponseEntity
			.status(CommonErrorCode.INVALID_REQUEST.httpStatus())
			.body(errorResponseFactory.from(CommonErrorCode.INVALID_REQUEST, errors));
	}

	private List<FieldErrorResponse> fieldErrors(BindingResult bindingResult) {
		return fieldErrorMapper.fromBindingResult(bindingResult);
	}

	private void logByLevel(ErrorCode errorCode, BusinessException exception) {
		switch (errorCode.logLevel()) {
			case DEBUG -> log.debug("{}: {}", errorCode.code(), exception.getSafeMessage());
			case INFO -> log.info("{}: {}", errorCode.code(), exception.getSafeMessage());
			case WARN -> log.warn("{}: {}", errorCode.code(), exception.getSafeMessage());
			case ERROR -> log.error("{}: {}", errorCode.code(), exception.getSafeMessage(), exception);
		}
	}
}

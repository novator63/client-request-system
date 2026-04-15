package com.example.app.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice(basePackages = "com.example.app")
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiErrorResponse> handleBusiness(
		BusinessException ex,
		HttpServletRequest request
	) {
		return build(ex.getStatus(), ex.getMessage(), request, List.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(
		MethodArgumentNotValidException ex,
		HttpServletRequest request
	) {
		List<ApiErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(this::toValidationError)
			.distinct()
			.toList();

		return build(HttpStatus.BAD_REQUEST, "Request validation failed", request, validationErrors);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
		ConstraintViolationException ex,
		HttpServletRequest request
	) {
		List<ApiErrorResponse.ValidationError> validationErrors = ex.getConstraintViolations()
			.stream()
			.map(violation -> new ApiErrorResponse.ValidationError(
				violation.getPropertyPath().toString(),
				violation.getMessage()
			))
			.distinct()
			.toList();

		return build(HttpStatus.BAD_REQUEST, "Request validation failed", request, validationErrors);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
		MethodArgumentTypeMismatchException ex,
		HttpServletRequest request
	) {
		String message = "Invalid value for parameter: " + ex.getName();
		return build(HttpStatus.BAD_REQUEST, message, request, List.of());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
		HttpMessageNotReadableException ex,
		HttpServletRequest request
	) {
		return build(HttpStatus.BAD_REQUEST, "Malformed request body", request, List.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(
		Exception ex,
		HttpServletRequest request
	) {
		log.error("Unhandled exception for request {}", request.getRequestURI(), ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request, List.of());
	}

	private ApiErrorResponse.ValidationError toValidationError(FieldError fieldError) {
		String fieldName = fieldError.getField();
		String message = fieldError.getDefaultMessage() != null
			? fieldError.getDefaultMessage()
			: "Invalid value";

		return new ApiErrorResponse.ValidationError(fieldName, message);
	}

	private ResponseEntity<ApiErrorResponse> build(
		HttpStatus status,
		String message,
		HttpServletRequest request,
		List<ApiErrorResponse.ValidationError> validationErrors
	) {
		ApiErrorResponse response = new ApiErrorResponse(
			Instant.now(),
			status.value(),
			status.getReasonPhrase(),
			message,
			request.getRequestURI(),
			validationErrors
		);

		return ResponseEntity.status(status).body(response);
	}
}

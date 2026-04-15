package com.example.app.common.error;

import com.example.app.auth.exception.InvalidCredentialsException;
import com.example.app.auth.exception.UnauthenticatedException;
import com.example.app.category.exception.CategoryBadRequestException;
import com.example.app.category.exception.CategoryConflictException;
import com.example.app.category.exception.CategoryNotFoundException;
import com.example.app.user.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice(basePackages = "com.example.app")
public class GlobalExceptionHandler {

	@ExceptionHandler(CategoryNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleCategoryNotFound(
		CategoryNotFoundException ex,
		HttpServletRequest request
	) {
		return build(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler(CategoryConflictException.class)
	public ResponseEntity<ApiErrorResponse> handleCategoryConflict(
		CategoryConflictException ex,
		HttpServletRequest request
	) {
		return build(HttpStatus.CONFLICT, "CATEGORY_NAME_CONFLICT", ex.getMessage(), request);
	}

	@ExceptionHandler(CategoryBadRequestException.class)
	public ResponseEntity<ApiErrorResponse> handleCategoryBadRequest(
		CategoryBadRequestException ex,
		HttpServletRequest request
	) {
		return build(HttpStatus.BAD_REQUEST, "CATEGORY_BAD_REQUEST", ex.getMessage(), request);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
		InvalidCredentialsException ex,
		HttpServletRequest request
	) {
		return build(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", ex.getMessage(), request);
	}

	@ExceptionHandler(UnauthenticatedException.class)
	public ResponseEntity<ApiErrorResponse> handleUnauthenticated(
		UnauthenticatedException ex,
		HttpServletRequest request
	) {
		return build(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHENTICATED", ex.getMessage(), request);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleUserNotFound(
		UserNotFoundException ex,
		HttpServletRequest request
	) {
		return build(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(
		MethodArgumentNotValidException ex,
		HttpServletRequest request
	) {
		FieldError fieldError = ex.getBindingResult().getFieldError();
		String message = (fieldError != null && fieldError.getDefaultMessage() != null)
			? fieldError.getDefaultMessage()
			: "Request validation failed";

		return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(
		Exception ex,
		HttpServletRequest request
	) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error", request);
	}

	private ResponseEntity<ApiErrorResponse> build(
		HttpStatus status,
		String code,
		String message,
		HttpServletRequest request
	) {
		ApiErrorResponse response = new ApiErrorResponse(
			Instant.now(),
			status.value(),
			status.getReasonPhrase(),
			code,
			message,
			request.getRequestURI()
		);

		return ResponseEntity.status(status).body(response);
	}
}

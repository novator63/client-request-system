package com.example.app.common.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
	Instant timestamp,
	int status,
	String error,
	String message,
	String path,
	List<ValidationError> validationErrors
) {
	public record ValidationError(
		String field,
		String message
	) {
	}
}

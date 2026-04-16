package com.example.app.auth.exception;

import com.example.app.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends BusinessException {

	public InvalidRefreshTokenException() {
		super(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
	}
}

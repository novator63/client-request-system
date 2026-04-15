package com.example.app.auth.exception;

import com.example.app.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BusinessException {

	public InvalidCredentialsException() {
		super(HttpStatus.UNAUTHORIZED, "Invalid email or password");
	}
}

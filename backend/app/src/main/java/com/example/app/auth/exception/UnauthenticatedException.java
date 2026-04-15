package com.example.app.auth.exception;

import com.example.app.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class UnauthenticatedException extends BusinessException {

	public UnauthenticatedException() {
		super(HttpStatus.UNAUTHORIZED, "Authentication required");
	}
}

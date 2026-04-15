package com.example.app.user.exception;

import com.example.app.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {

	public UserNotFoundException() {
		super(HttpStatus.NOT_FOUND, "User not found");
	}
}

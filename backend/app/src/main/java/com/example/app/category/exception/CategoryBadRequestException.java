package com.example.app.category.exception;

import com.example.app.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class CategoryBadRequestException extends BusinessException {

	public CategoryBadRequestException(String message) {
		super(HttpStatus.BAD_REQUEST, message);
	}
}

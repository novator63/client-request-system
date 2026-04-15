package com.example.app.category.exception;

import com.example.app.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class CategoryConflictException extends BusinessException {

	public CategoryConflictException() {
		super(HttpStatus.CONFLICT, "Category name already exists");
	}
}

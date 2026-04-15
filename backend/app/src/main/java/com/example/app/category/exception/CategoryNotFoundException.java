package com.example.app.category.exception;

import com.example.app.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends BusinessException {

	public CategoryNotFoundException() {
		super(HttpStatus.NOT_FOUND, "Category not found");
	}
}

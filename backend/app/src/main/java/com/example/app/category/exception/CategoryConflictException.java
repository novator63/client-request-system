package com.example.app.category.exception;

public class CategoryConflictException extends RuntimeException {

	public CategoryConflictException() {
		super("Category name already exists");
	}
}

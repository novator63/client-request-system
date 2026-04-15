package com.example.app.category.exception;

public class CategoryNotFoundException extends RuntimeException {

	public CategoryNotFoundException() {
		super("Category not found");
	}
}

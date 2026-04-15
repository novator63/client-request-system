package com.example.app.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
	@NotBlank(message = "Category name is required")
	@Size(max = 150, message = "Category name must be at most 150 characters")
	String name,

	@Size(max = 2000, message = "Category description must be at most 2000 characters")
	String description
) {
}

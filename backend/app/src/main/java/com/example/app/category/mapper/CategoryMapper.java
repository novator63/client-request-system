package com.example.app.category.mapper;

import com.example.app.category.dto.request.CreateCategoryRequest;
import com.example.app.category.dto.request.UpdateCategoryRequest;
import com.example.app.category.dto.response.CategoryResponse;
import com.example.app.category.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

	public Category toEntity(CreateCategoryRequest request) {
		Category category = new Category();
		category.setName(normalizeText(request.name()));
		category.setDescription(normalizeText(request.description()));
		return category;
	}

	public void applyUpdate(Category category, UpdateCategoryRequest request) {
		category.setName(normalizeText(request.name()));
		category.setDescription(normalizeText(request.description()));
	}

	public CategoryResponse toResponse(Category category) {
		return new CategoryResponse(
			category.getId(),
			category.getName(),
			category.getDescription(),
			category.getCreatedAt()
		);
	}

	private String normalizeText(String value) {
		if (value == null) {
			return null;
		}

		String normalized = value.trim();
		return normalized.isEmpty() ? null : normalized;
	}
}

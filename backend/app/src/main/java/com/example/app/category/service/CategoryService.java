package com.example.app.category.service;

import com.example.app.category.dto.request.CreateCategoryRequest;
import com.example.app.category.dto.request.UpdateCategoryRequest;
import com.example.app.category.dto.response.CategoryResponse;
import com.example.app.category.entity.Category;
import com.example.app.category.exception.CategoryBadRequestException;
import com.example.app.category.exception.CategoryConflictException;
import com.example.app.category.exception.CategoryNotFoundException;
import com.example.app.category.mapper.CategoryMapper;
import com.example.app.category.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final CategoryMapper categoryMapper;

	public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
		this.categoryRepository = categoryRepository;
		this.categoryMapper = categoryMapper;
	}

	@Transactional
	public CategoryResponse create(CreateCategoryRequest request) {
		String categoryName = normalizeRequiredName(request.name());
		ensureNameIsUniqueForCreate(categoryName);

		Category category = categoryMapper.toEntity(request);
		category.setName(categoryName);

		Category savedCategory = categoryRepository.save(category);
		return categoryMapper.toResponse(savedCategory);
	}

	public List<CategoryResponse> getAll() {
		return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
			.stream()
			.map(categoryMapper::toResponse)
			.toList();
	}

	public CategoryResponse getById(Long id) {
		Category category = findCategoryById(id);
		return categoryMapper.toResponse(category);
	}

	@Transactional
	public CategoryResponse update(Long id, UpdateCategoryRequest request) {
		Category category = findCategoryById(id);
		String categoryName = normalizeRequiredName(request.name());
		ensureNameIsUniqueForUpdate(categoryName, id);

		categoryMapper.applyUpdate(category, request);
		category.setName(categoryName);

		Category savedCategory = categoryRepository.save(category);
		return categoryMapper.toResponse(savedCategory);
	}

	@Transactional
	public void delete(Long id) {
		if (!categoryRepository.existsById(id)) {
			throw new CategoryNotFoundException();
		}

		categoryRepository.deleteById(id);
	}

	private Category findCategoryById(Long id) {
		return categoryRepository.findById(id)
			.orElseThrow(CategoryNotFoundException::new);
	}

	private void ensureNameIsUniqueForCreate(String categoryName) {
		if (categoryRepository.existsByName(categoryName)) {
			throw new CategoryConflictException();
		}
	}

	private void ensureNameIsUniqueForUpdate(String categoryName, Long id) {
		if (categoryRepository.existsByNameAndIdNot(categoryName, id)) {
			throw new CategoryConflictException();
		}
	}

	private String normalizeRequiredName(String value) {
		if (value == null) {
			throw new CategoryBadRequestException("Category name is required");
		}

		String normalized = value.trim();
		if (normalized.isEmpty()) {
			throw new CategoryBadRequestException("Category name is required");
		}

		return normalized;
	}
}

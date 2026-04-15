package com.example.app.category.controller;

import com.example.app.category.dto.request.CreateCategoryRequest;
import com.example.app.category.dto.request.UpdateCategoryRequest;
import com.example.app.category.dto.response.CategoryResponse;
import com.example.app.category.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/categories")
@Validated
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@PostMapping
	@ResponseStatus(CREATED)
	public CategoryResponse create(@Valid @RequestBody CreateCategoryRequest request) {
		return categoryService.create(request);
	}

	@GetMapping
	public List<CategoryResponse> getAll() {
		return categoryService.getAll();
	}

	@GetMapping("/{id}")
	public CategoryResponse getById(@PathVariable @Positive(message = "Category id must be positive") Long id) {
		return categoryService.getById(id);
	}

	@PutMapping("/{id}")
	public CategoryResponse update(
		@PathVariable @Positive(message = "Category id must be positive") Long id,
		@Valid @RequestBody UpdateCategoryRequest request
	) {
		return categoryService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(NO_CONTENT)
	public void delete(@PathVariable @Positive(message = "Category id must be positive") Long id) {
		categoryService.delete(id);
	}
}

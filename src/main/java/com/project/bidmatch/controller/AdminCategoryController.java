package com.project.bidmatch.controller;

import com.project.bidmatch.dto.CategoryCreateRequest;
import com.project.bidmatch.dto.CategoryResponse;
import com.project.bidmatch.dto.CategoryUpdateRequest;
import com.project.bidmatch.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

  private final CategoryService categoryService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CategoryResponse create(@Validated @RequestBody CategoryCreateRequest request) {
    return categoryService.createCategory(request);
  }

  @PutMapping("/{id}")
  public CategoryResponse update(@PathVariable Long id, @Validated @RequestBody CategoryUpdateRequest request) {
    return categoryService.updateCategory(id, request);
  }
}

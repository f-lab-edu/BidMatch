package com.project.bidmatch.controller;

import com.project.bidmatch.dto.CategoryResponse;
import com.project.bidmatch.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  public List<CategoryResponse> getAllCategories() {
    return categoryService.findAllCategories();
  }

  @GetMapping("/{id}")
  public CategoryResponse getCategoryById(@PathVariable Long id) {
    return categoryService.findCategoryById(id);
  }
}

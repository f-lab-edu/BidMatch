package com.project.bidmatch.service;

import com.project.bidmatch.common.exception.BusinessException;
import com.project.bidmatch.common.exception.ErrorCode;
import com.project.bidmatch.domain.category.Category;
import com.project.bidmatch.dto.CategoryCreateRequest;
import com.project.bidmatch.dto.CategoryResponse;
import com.project.bidmatch.dto.CategoryUpdateRequest;
import com.project.bidmatch.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  @Transactional
  public CategoryResponse createCategory(CategoryCreateRequest request) {
    Category category;
    if (request.parentId() == null) {
      if (categoryRepository.existsByParentIsNullAndName(request.name())) {
        throw new BusinessException(ErrorCode.DUPLICATED_CATEGORY_NAME);
      }
      category = Category.createRoot(request.name());
    } else {
      Category parent = categoryRepository.findById(request.parentId())
          .orElseThrow(() -> new BusinessException(ErrorCode.PARENT_CATEGORY_NOT_FOUND));
      if (categoryRepository.existsByParentAndName(parent, request.name())) {
        throw new BusinessException(ErrorCode.DUPLICATED_CATEGORY_NAME);
      }
      category = Category.createChild(request.name(), parent);
    }

    Category saved = categoryRepository.save(category);
    return CategoryResponse.from(saved);
  }

  @Transactional
  public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

    if (!category.getName().equals(request.name())) {
      boolean duplicated =
          category.isRoot() ? categoryRepository.existsByParentIsNullAndName(request.name())
              : categoryRepository.existsByParentAndName(category.getParent(), request.name());
      if (duplicated) {
        throw new BusinessException(ErrorCode.DUPLICATED_CATEGORY_NAME);
      }
    }

    category.updateName(request.name());
    return CategoryResponse.from(category);
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> findAllCategories() {
    return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
  }

  @Transactional(readOnly = true)
  public CategoryResponse findCategoryById(Long id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    return CategoryResponse.from(category);
  }
}

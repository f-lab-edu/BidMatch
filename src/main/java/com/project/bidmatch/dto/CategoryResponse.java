package com.project.bidmatch.dto;

import com.project.bidmatch.domain.category.Category;
import java.time.LocalDateTime;

public record CategoryResponse(
    Long id,
    String name,
    Long parentId,
    LocalDateTime createdAt
) {
  public static CategoryResponse from(Category category) {
    Long parentId = category.getParent() == null ? null : category.getParent().getId();
    return new CategoryResponse(
        category.getId(),
        category.getName(),
        parentId,
        category.getCreatedAt()
    );
  }
}
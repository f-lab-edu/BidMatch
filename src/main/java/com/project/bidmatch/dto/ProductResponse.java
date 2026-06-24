package com.project.bidmatch.dto;

import com.project.bidmatch.domain.product.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
    Long id,
    String name,
    Long brandId,
    String brandName,
    Long categoryId,
    String categoryName,
    String modelNumber,
    BigDecimal releasePrice,
    String imageUrl,
    LocalDateTime createdAt
) {
  public static ProductResponse from(Product product) {
    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getBrand().getId(),
        product.getBrand().getName(),
        product.getCategory().getId(),
        product.getCategory().getName(),
        product.getModelNumber(),
        product.getReleasePrice(),
        product.getImageUrl(),
        product.getCreatedAt()
    );
  }
}

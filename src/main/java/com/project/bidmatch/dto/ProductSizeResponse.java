package com.project.bidmatch.dto;

import com.project.bidmatch.domain.product.ProductSize;
import java.time.LocalDateTime;

public record ProductSizeResponse(
    Long id,
    Long productId,
    String size,
    LocalDateTime createdAt
) {

  public static ProductSizeResponse from(ProductSize productSize) {
    return new ProductSizeResponse(
        productSize.getId(),
        productSize.getProduct().getId(),
        productSize.getSize(),
        productSize.getCreatedAt()
    );
  }
}

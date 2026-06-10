package com.project.bidmatch.dto;

import com.project.bidmatch.domain.brand.Brand;
import java.time.LocalDateTime;

public record BrandResponse(Long id, String name, String englishName, String logoUrl,
                            LocalDateTime createdAt) {
  public static BrandResponse from(Brand brand) {
    return new BrandResponse(brand.getId(), brand.getName(), brand.getEnglishName(),
        brand.getLogoUrl(), brand.getCreatedAt());
  }

}

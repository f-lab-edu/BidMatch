package com.project.bidmatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record ProductCreateRequest(
    @NotBlank(message = "이름은 필수입니다") String name,
    @NotNull(message = "브랜드는 필수입니다") Long brandId,
    @NotNull(message = "카테고리는 필수입니다") Long categoryId,
    @NotBlank(message = "모델번호는 필수입니다") String modelNumber,
    @NotNull(message = "출시가는 필수입니다")
    @PositiveOrZero(message = "출시가는 0 이상이어야 합니다") BigDecimal releasePrice,
    String imageUrl
) {

}

package com.project.bidmatch.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductSizeCreateRequest(
    @NotBlank(message = "사이즈는 필수입니다") String size
) {

}

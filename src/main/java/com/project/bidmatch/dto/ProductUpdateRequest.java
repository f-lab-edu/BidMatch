package com.project.bidmatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(
    @NotBlank(message = "이름은 필수입니다") String name,
    @Size(max = 500, message = "이미지 URL은 최대 500자 입니다") String imageUrl
) {

}

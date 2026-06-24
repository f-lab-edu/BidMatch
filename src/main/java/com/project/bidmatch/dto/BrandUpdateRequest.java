package com.project.bidmatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BrandUpdateRequest(@NotBlank(message = "이름은 필수입니다") String name, @NotBlank(message = "영어 이름은 필수입니다") String englishName,
                                 @Size(max = 500, message = "최대 500자 입니다") String logoUrl) {

}

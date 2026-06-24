package com.project.bidmatch.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequest(
    @NotBlank(message = "이름은 필수입니다")
    String name,
    Long parentId
) {

}

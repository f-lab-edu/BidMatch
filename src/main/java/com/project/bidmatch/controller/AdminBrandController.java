package com.project.bidmatch.controller;

import com.project.bidmatch.dto.BrandCreateRequest;
import com.project.bidmatch.dto.BrandResponse;
import com.project.bidmatch.dto.BrandUpdateRequest;
import com.project.bidmatch.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@RequestMapping("/api/admin/brands")
public class AdminBrandController {

  private final BrandService brandService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BrandResponse create(@Validated @RequestBody BrandCreateRequest brandCreateRequest) {
    return brandService.createBrand(brandCreateRequest);
  }

  @PutMapping("/{id}")
  public BrandResponse update(@PathVariable Long id, @Validated @RequestBody BrandUpdateRequest brandUpdateRequest) {
    return brandService.updateBrand(id, brandUpdateRequest);
  }
}

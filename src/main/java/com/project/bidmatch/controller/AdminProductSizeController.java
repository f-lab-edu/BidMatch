package com.project.bidmatch.controller;

import com.project.bidmatch.dto.ProductSizeCreateRequest;
import com.project.bidmatch.dto.ProductSizeResponse;
import com.project.bidmatch.service.ProductSizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@RequestMapping("/api/admin/products/{productId}/sizes")
public class AdminProductSizeController {

  private final ProductSizeService productSizeService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProductSizeResponse create(@PathVariable Long productId, @Validated @RequestBody
  ProductSizeCreateRequest request) {
    return productSizeService.addSize(productId, request);
  }
}

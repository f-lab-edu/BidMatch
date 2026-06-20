package com.project.bidmatch.controller;

import com.project.bidmatch.dto.ProductCreateRequest;
import com.project.bidmatch.dto.ProductResponse;
import com.project.bidmatch.dto.ProductUpdateRequest;
import com.project.bidmatch.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/admin/products")
public class AdminProductController {

  private final ProductService productService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProductResponse create(@Validated @RequestBody ProductCreateRequest request) {
    return productService.createProduct(request);
  }

  @PutMapping("/{id}")
  public ProductResponse update(@PathVariable Long id,
      @Validated @RequestBody ProductUpdateRequest request) {
    return productService.updateProduct(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    productService.deleteProduct(id);
  }
}

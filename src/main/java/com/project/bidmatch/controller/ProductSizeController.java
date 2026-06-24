package com.project.bidmatch.controller;

import com.project.bidmatch.dto.ProductSizeResponse;
import com.project.bidmatch.service.ProductSizeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/{productId}/sizes")
public class ProductSizeController {

  private final ProductSizeService productSizeService;

  @GetMapping
  public List<ProductSizeResponse> getSizes(@PathVariable Long productId) {
    return productSizeService.findSizes(productId);
  }
}

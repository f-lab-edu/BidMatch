package com.project.bidmatch.controller;

import com.project.bidmatch.dto.ProductResponse;
import com.project.bidmatch.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

  private final ProductService productService;

  @GetMapping
  public List<ProductResponse> getAllProducts() {
    return productService.findAllProducts();
  }

  @GetMapping("/{id}")
  public ProductResponse getProductById(@PathVariable Long id) {
    return productService.findProductById(id);
  }
}

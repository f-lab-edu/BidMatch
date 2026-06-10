package com.project.bidmatch.controller;

import com.project.bidmatch.dto.BrandResponse;
import com.project.bidmatch.service.BrandService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/brands")
public class BrandController {

  private final BrandService brandService;

  @GetMapping
  public List<BrandResponse> getAllBrands() {
    return brandService.findAllBrands();
  }

  @GetMapping("/{id}")
  public BrandResponse getBrandById(@PathVariable Long id) {
    return brandService.findBrandById(id);
  }
}

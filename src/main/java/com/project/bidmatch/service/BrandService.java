package com.project.bidmatch.service;

import com.project.bidmatch.common.exception.BusinessException;
import com.project.bidmatch.common.exception.ErrorCode;
import com.project.bidmatch.domain.brand.Brand;
import com.project.bidmatch.dto.BrandCreateRequest;
import com.project.bidmatch.dto.BrandResponse;
import com.project.bidmatch.dto.BrandUpdateRequest;
import com.project.bidmatch.repository.BrandRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandService {

  private final BrandRepository brandRepository;

  @Transactional
  public BrandResponse createBrand(BrandCreateRequest brandCreateRequest) {
    if (brandRepository.existsByName(brandCreateRequest.name())) {
      throw new BusinessException(ErrorCode.DUPLICATED_BRAND_NAME);
    }
    Brand brand = Brand.createBrand(
        brandCreateRequest.name(),
        brandCreateRequest.englishName(),
        brandCreateRequest.logoUrl()
    );

    Brand save = brandRepository.save(brand);

    return BrandResponse.from(save);
  }

  @Transactional
  public BrandResponse updateBrand(Long id, BrandUpdateRequest brandUpdateRequest) {
    Brand brand = brandRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.BRAND_NOT_FOUND));
    if (!brand.getName().equals(brandUpdateRequest.name()) && brandRepository.existsByName(
        brandUpdateRequest.name())) {
      throw new BusinessException(ErrorCode.DUPLICATED_BRAND_NAME);
    }
    brand.updateBrand(brandUpdateRequest.name(), brandUpdateRequest.englishName(),
        brandUpdateRequest.logoUrl());
    return BrandResponse.from(brand);
  }

  @Transactional(readOnly = true)
  public List<BrandResponse> findAllBrands() {
    return brandRepository.findAll().stream().map(x -> BrandResponse.from(x)).toList();
  }

  @Transactional(readOnly = true)
  public BrandResponse findBrandById(Long id) {
    Brand brand = brandRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.BRAND_NOT_FOUND));
    return BrandResponse.from(brand);
  }
}

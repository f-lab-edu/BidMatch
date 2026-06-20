package com.project.bidmatch.service;

import com.project.bidmatch.common.exception.BusinessException;
import com.project.bidmatch.common.exception.ErrorCode;
import com.project.bidmatch.domain.brand.Brand;
import com.project.bidmatch.domain.category.Category;
import com.project.bidmatch.domain.product.Product;
import com.project.bidmatch.domain.product.ProductStatus;
import com.project.bidmatch.dto.ProductCreateRequest;
import com.project.bidmatch.dto.ProductResponse;
import com.project.bidmatch.dto.ProductUpdateRequest;
import com.project.bidmatch.repository.BrandRepository;
import com.project.bidmatch.repository.CategoryRepository;
import com.project.bidmatch.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final BrandRepository brandRepository;
  private final CategoryRepository categoryRepository;

  @Transactional
  public ProductResponse createProduct(ProductCreateRequest request) {
    if (productRepository.existsByModelNumber(request.modelNumber())) {
      throw new BusinessException(ErrorCode.DUPLICATED_MODEL_NUMBER);
    }

    Brand brand = brandRepository.findById(request.brandId())
        .orElseThrow(() -> new BusinessException(ErrorCode.BRAND_NOT_FOUND));

    Category category = categoryRepository.findById(request.categoryId())
        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

    if (categoryRepository.existsByParent(category)) {
      throw new BusinessException(ErrorCode.NOT_LEAF_CATEGORY);
    }

    Product product = Product.create(
        request.name(),
        brand,
        category,
        request.modelNumber(),
        request.releasePrice(),
        request.imageUrl()
    );

    return ProductResponse.from(productRepository.save(product));
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> findAllProducts() {
    return productRepository.findByStatus(ProductStatus.ACTIVE).stream()
        .map(ProductResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public ProductResponse findProductById(Long id) {
    return ProductResponse.from(findActiveProduct(id));
  }

  @Transactional
  public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
    Product product = findActiveProduct(id);
    product.update(request.name(), request.imageUrl());
    return ProductResponse.from(product);
  }

  @Transactional
  public void deleteProduct(Long id) {
    Product product = findActiveProduct(id);
    product.deactivate();
  }

  // INACTIVE는 "없는 것"으로 취급 -> 404
  private Product findActiveProduct(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    if(!product.isActive()) {
      throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
    }
    return product;
  }
}
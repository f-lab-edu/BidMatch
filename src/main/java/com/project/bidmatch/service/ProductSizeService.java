package com.project.bidmatch.service;

import com.project.bidmatch.common.exception.BusinessException;
import com.project.bidmatch.common.exception.ErrorCode;
import com.project.bidmatch.domain.product.Product;
import com.project.bidmatch.domain.product.ProductSize;
import com.project.bidmatch.domain.product.ProductStatus;
import com.project.bidmatch.dto.ProductSizeCreateRequest;
import com.project.bidmatch.dto.ProductSizeResponse;
import com.project.bidmatch.repository.ProductRepository;
import com.project.bidmatch.repository.ProductSizeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductSizeService {

  private final ProductSizeRepository productSizeRepository;
  private final ProductRepository productRepository;

  @Transactional
  public ProductSizeResponse addSize(Long productId, ProductSizeCreateRequest request) {
    Product product = findActiveProduct(productId);

    if(productSizeRepository.existsByProductAndSize(product, request.size())) {
      throw new BusinessException(ErrorCode.DUPLICATED_PRODUCT_SIZE);
    }

    ProductSize productSize = ProductSize.create(product, request.size());
    return ProductSizeResponse.from(productSizeRepository.save(productSize));
  }

  @Transactional(readOnly = true)
  public List<ProductSizeResponse> findSizes(Long productId) {
    Product product = findActiveProduct(productId);
    return productSizeRepository.findByProductAndStatus(product, ProductStatus.ACTIVE).stream()
        .map(ProductSizeResponse::from)
        .toList();
  }

  @Transactional
  public void deleteSize(Long productId, Long sizeId) {
    ProductSize size = productSizeRepository.findById(sizeId)
        .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_SIZE_NOT_FOUND));
    // 경로의 productId와 실제 소속이 다르거나 이미 비활성이면 없는 것으로 취급
    if(!size.getProduct().getId().equals(productId) || !size.isActive()) {
      throw new BusinessException(ErrorCode.PRODUCT_SIZE_NOT_FOUND);
    }
    size.deactivate();
  }

  private Product findActiveProduct(Long productId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    if(!product.isActive()) {
      throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
    }
    return product;
  }
}

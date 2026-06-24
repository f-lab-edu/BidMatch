package com.project.bidmatch.service;

import com.project.bidmatch.common.exception.BusinessException;
import com.project.bidmatch.common.exception.ErrorCode;
import com.project.bidmatch.domain.product.Product;
import com.project.bidmatch.domain.product.ProductSize;
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
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

    if (productSizeRepository.existsByProductAndSize(product, request.size())) {
      throw new BusinessException(ErrorCode.DUPLICATED_PRODUCT_SIZE);
    }

    ProductSize productSize = ProductSize.create(product, request.size());
    return ProductSizeResponse.from(productSizeRepository.save(productSize));
  }

  @Transactional(readOnly = true)
  public List<ProductSizeResponse> findSizes(Long productId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

    return productSizeRepository.findByProduct(product).stream()
        .map(ProductSizeResponse::from)
        .toList();
  }
}

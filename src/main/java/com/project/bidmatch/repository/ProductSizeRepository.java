package com.project.bidmatch.repository;

import com.project.bidmatch.domain.product.Product;
import com.project.bidmatch.domain.product.ProductSize;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSizeRepository extends JpaRepository<ProductSize, Long> {

  boolean existsByProductAndSize(Product product, String size);

  List<ProductSize> findByProduct(Product product);
}

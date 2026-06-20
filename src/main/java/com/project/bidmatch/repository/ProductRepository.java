package com.project.bidmatch.repository;

import com.project.bidmatch.domain.product.Product;
import com.project.bidmatch.domain.product.ProductStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

  boolean existsByModelNumber(String modelNumber);

  List<Product> findByStatus(ProductStatus status);
}

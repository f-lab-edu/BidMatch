package com.project.bidmatch.repository;

import com.project.bidmatch.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

  boolean existsByModelNumber(String modelNumber);
}

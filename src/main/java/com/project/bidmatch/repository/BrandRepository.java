package com.project.bidmatch.repository;

import com.project.bidmatch.domain.brand.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
  boolean existsByName(String name);
}

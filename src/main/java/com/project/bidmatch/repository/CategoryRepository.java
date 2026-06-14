package com.project.bidmatch.repository;

import com.project.bidmatch.domain.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  boolean existsByParentIsNullAndName(String name);

  boolean existsByParentAndName(Category parent, String name);
}

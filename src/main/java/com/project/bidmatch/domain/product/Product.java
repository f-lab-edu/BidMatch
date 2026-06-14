package com.project.bidmatch.domain.product;

import com.project.bidmatch.common.BaseEntity;
import com.project.bidmatch.domain.brand.Brand;
import com.project.bidmatch.domain.category.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "brand_id", nullable = false)
  private Brand brand;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Column(name = "model_number", nullable = false, unique = true)
  private String modelNumber;

  @Column(name = "release_price", nullable = false)
  private BigDecimal releasePrice;

  @Column(name = "image_url")
  private String imageUrl;

  @Builder
  private Product(String name, Brand brand, Category category, String modelNumber,
      BigDecimal releasePrice, String imageUrl) {
    this.name = name;
    this.brand = brand;
    this.category = category;
    this.modelNumber = modelNumber;
    this.releasePrice = releasePrice;
    this.imageUrl = imageUrl;
  }

  public static Product create(String name, Brand brand, Category category, String modelNumber,
      BigDecimal releasePrice, String imageUrl) {
    return Product.builder()
        .name(name)
        .brand(brand)
        .category(category)
        .modelNumber(modelNumber)
        .releasePrice(releasePrice)
        .imageUrl(imageUrl)
        .build();
  }
}

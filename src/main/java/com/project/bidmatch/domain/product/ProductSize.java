package com.project.bidmatch.domain.product;

import com.project.bidmatch.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "product_sizes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_product_size",
        columnNames = {"product_id", "size"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductSize extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(nullable = false)
  private String size;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProductStatus status;

  @Builder
  private ProductSize(Product product, String size) {
    this.product = product;
    this.size = size;
    this.status = ProductStatus.ACTIVE;
  }

  public void deactivate() {
    this.status = ProductStatus.INACTIVE;
  }

  public boolean isActive() {
    return this.status == ProductStatus.ACTIVE;
  }

  public static ProductSize create(Product product, String size) {
    return ProductSize.builder()
        .product(product)
        .size(size)
        .build();
  }
}

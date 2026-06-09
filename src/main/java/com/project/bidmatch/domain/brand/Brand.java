package com.project.bidmatch.domain.brand;

import com.project.bidmatch.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "brands")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brand extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private String englishName;

  @Column(length = 500)
  private String logoUrl;

  @Builder
  private Brand(String name, String englishName, String logoUrl) {
    this.name = name;
    this.englishName = englishName;
    this.logoUrl = logoUrl;
  }


  static Brand createBrand(String name, String englishName, String logoUrl) {
    return Brand.builder()
        .name(name)
        .englishName(englishName)
        .logoUrl(logoUrl)
        .build();
  }

  public void updateBrand(Brand brand) {
    this.name = brand.getName();
    this.englishName = brand.getEnglishName();
    this.logoUrl = brand.getLogoUrl();
  }
}

package com.project.bidmatch.domain.category;

import com.project.bidmatch.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Category parent;

  @Builder
  private Category(String name, Category parent) {
    this.name = name;
    this.parent = parent;
  }

  public static Category createRoot(String name) {
    return Category.builder().name(name).build();
  }

  public static Category createChild(String name, Category parent) {
    return Category.builder().name(name).parent(parent).build();
  }

  public void updateName(String name) {
    this.name = name;
  }

  public boolean isRoot() {
    return parent == null;
  }
}

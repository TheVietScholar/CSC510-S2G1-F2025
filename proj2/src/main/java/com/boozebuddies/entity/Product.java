package com.boozebuddies.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(precision = 10, scale = 2, nullable = false)
  private BigDecimal price;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "merchant_id", nullable = false)
  private Merchant merchant;

  @Column(name = "is_alcohol")
  private boolean isAlcohol;

  public boolean isAlcohol() {  // This should be the getter name
    return isAlcohol;
  }

  public void setAlcohol(boolean alcohol) {  // Setter
    this.isAlcohol = alcohol;
  }

  @Column(name = "alcohol_content")
  private Double alcoholContent;

  @Builder.Default private boolean available = true;

  @Column(name = "image_url")
  private String imageUrl;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
  @Builder.Default
  private List<OrderItem> orderItems = new ArrayList<>();

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Rating> ratings = new ArrayList<>();

  @Column(name = "volume_ml")
  private Integer volume; // in milliliters

  public boolean isAvailable() {
    return available;
  }
}

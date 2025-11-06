package com.boozebuddies.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/** Entity representing a product available for purchase. */
@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Product {
  /** The unique product ID */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** The name of the product */
  @Column(nullable = false)
  private String name;

  /** A description of the product */
  private String description;

  /** The price of the product */
  @Column(precision = 10, scale = 2, nullable = false)
  private BigDecimal price;

  /** The category this product belongs to */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  /** The merchant selling this product */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "merchant_id", nullable = false)
  private Merchant merchant;

  /** Whether this product contains alcohol */
  @Builder.Default
  @Column(name = "is_alcohol")
  private boolean isAlcohol = false;

  public boolean isAlcohol() { // This should be the getter name
    return isAlcohol;
  }

  public void setAlcohol(boolean alcohol) { // Setter
    this.isAlcohol = alcohol;
  }

  /** The alcohol content percentage (if applicable) */
  @Column(name = "alcohol_content")
  private Double alcoholContent;

  // @Builder.Default
  // @Column(name = "stock_quantity")
  // private Integer stockQuantity = 0;

  /** Whether the product is currently available for purchase */
  @Builder.Default private boolean available = true;

  /** URL to the product image */
  @Column(name = "image_url")
  private String imageUrl;

  /** The list of order items that include this product */
  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
  @Builder.Default
  private List<OrderItem> orderItems = new ArrayList<>();

  /** The list of ratings received by this product */
  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Rating> ratings = new ArrayList<>();

  /** The volume of the product in milliliters */
  @Column(name = "volume_ml")
  private Integer volume;

  /**
   * Checks if the product is currently available for purchase.
   *
   * @return true if the product is available, false otherwise
   */
  public boolean isAvailable() {
    return available;
  }
}

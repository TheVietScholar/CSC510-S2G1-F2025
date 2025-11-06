package com.boozebuddies.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

/** Entity representing an item in an order. */
@Entity
@Table(name = "order_items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OrderItem {
  /** The unique order item ID */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** The order this item belongs to */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  /** The product being ordered (nullable to preserve history if product is removed) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  /** The line number of this item in the order */
  @Column(name = "line_no", nullable = false)
  private Integer lineNo;

  /** Snapshot of the product name at time of order */
  @Column(nullable = false)
  private String name;

  /** The unit price of the product at time of order */
  @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
  private BigDecimal unitPrice;

  /** The quantity of the product ordered */
  @Column(nullable = false)
  private Integer quantity;

  /** The subtotal for this order item */
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal subtotal;

  /** Calculates and sets the subtotal before persisting or updating. */
  @PrePersist
  @PreUpdate
  public void calculateSubtotal() {
    if (unitPrice != null && quantity != null) {
      this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
  }
}

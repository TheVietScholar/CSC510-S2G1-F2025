package com.boozebuddies.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "order_items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
  private BigDecimal unitPrice;

  @Column(name = "subtotal", precision = 10, scale = 2)
  private BigDecimal subtotal;

  @PrePersist
  @PreUpdate
  public void calculateSubtotal() {
    if (unitPrice != null && quantity != null) {
      this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
  }
}

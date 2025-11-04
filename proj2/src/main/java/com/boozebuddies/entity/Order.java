package com.boozebuddies.entity;

import com.boozebuddies.model.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "merchant_id", nullable = false)
  private Merchant merchant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "driver_id")
  private Driver driver;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @Column(name = "total_amount", precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "delivery_address", nullable = false)
  private String deliveryAddress;

  @Column(name = "special_instructions")
  private String specialInstructions;

  @Builder.Default
  @Column(name = "age_verified")
  private boolean ageVerified = false;

  @Builder.Default
  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default
  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Column(name = "estimated_delivery_time")
  private LocalDateTime estimatedDeliveryTime;

  @Column(name = "promo_code")
  private String promoCode;

  // Order.java
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderColumn(name = "line_no")
  @BatchSize(size = 50) // reduce round trips when loading items
  private List<OrderItem> items;

  @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
  private Delivery delivery;

  @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
  private Payment payment;

  public void addItem(OrderItem item) {
    item.setOrder(this); // maintain both sides
    item.setLineNo(items.size() + 1);
    items.add(item);
  }

  public void removeItem(OrderItem item) {
    items.remove(item);
    item.setOrder(null);
    // re-normalize line numbers if you care about strict sequence:
    for (int i = 0; i < items.size(); i++) items.get(i).setLineNo(i + 1);
  }

  public void calculateTotal() {
    this.totalAmount =
        items.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public boolean canBeCancelled() {
    return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
  }

  public boolean isValidStatusTransition(OrderStatus newStatus) {
    // Implement your state transition logic here
    return true; // Simplified for example
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}

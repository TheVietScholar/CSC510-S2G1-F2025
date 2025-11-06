package com.boozebuddies.entity;

import com.boozebuddies.model.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.BatchSize;

/** Entity representing a customer order. */
@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Order {
  /** The unique order ID */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** The user who placed the order */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /** The merchant fulfilling the order */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "merchant_id", nullable = false)
  private Merchant merchant;

  /** The driver assigned to deliver the order */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "driver_id")
  private Driver driver;

  /** The current order status */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  /** The total amount of the order */
  @Column(name = "total_amount", precision = 10, scale = 2)
  private BigDecimal totalAmount;

  /** The delivery address for the order */
  @Column(name = "delivery_address", nullable = false)
  private String deliveryAddress;

  /** Any special instructions for the order */
  @Column(name = "special_instructions")
  private String specialInstructions;

  /** Whether age has been verified for alcohol orders */
  @Builder.Default
  @Column(name = "age_verified")
  private boolean ageVerified = false;

  /** When the order was created */
  @Builder.Default
  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  /** When the order was last updated */
  @Builder.Default
  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  /** The estimated time of delivery */
  @Column(name = "estimated_delivery_time")
  private LocalDateTime estimatedDeliveryTime;

  /** The promo code applied to the order */
  @Column(name = "promo_code")
  private String promoCode;

  /** The list of items in the order */
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderColumn(name = "line_no")
  @BatchSize(size = 50) // reduce round trips when loading items
  private List<OrderItem> items;

  /** The delivery information for the order */
  @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
  private Delivery delivery;

  /** The payment information for the order */
  @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
  private Payment payment;

  /**
   * Adds an item to the order.
   *
   * @param item the order item to add
   */
  public void addItem(OrderItem item) {
    item.setOrder(this); // maintain both sides
    item.setLineNo(items.size() + 1);
    items.add(item);
  }

  /**
   * Removes an item from the order.
   *
   * @param item the order item to remove
   */
  public void removeItem(OrderItem item) {
    items.remove(item);
    item.setOrder(null);
    // re-normalize line numbers if you care about strict sequence:
    for (int i = 0; i < items.size(); i++) items.get(i).setLineNo(i + 1);
  }

  /** Calculates and sets the total amount of the order based on item subtotals. */
  public void calculateTotal() {
    if (items == null || items.isEmpty()) {
      this.totalAmount = BigDecimal.ZERO;
      return;
    }

    // Filter out null subtotals and sum them
    this.totalAmount =
        items.stream()
            .map(OrderItem::getSubtotal)
            .filter(subtotal -> subtotal != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Checks if the order can be cancelled.
   *
   * @return true if the order is in PENDING or CONFIRMED status, false otherwise
   */
  public boolean canBeCancelled() {
    return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
  }

  /**
   * Validates if a status transition is allowed.
   *
   * @param newStatus the new status to transition to
   * @return true if the transition is valid, false otherwise
   */
  public boolean isValidStatusTransition(OrderStatus newStatus) {
    // Implement your state transition logic here
    return true; // Simplified for example
  }

  /** Updates the updatedAt timestamp before persisting changes. */
  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}

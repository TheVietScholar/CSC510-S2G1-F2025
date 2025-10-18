package com.boozebuddies.entity;

import lombok.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import com.boozebuddies.model.OrderStatus;

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

  /** Customer placing the order */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /** Restaurant fulfilling */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "merchant_id", nullable = false)
  private Merchant merchant;

  /**
   * (Optional) Driver directly on Order (you also have Delivery entity; keeping
   * this for compatibility)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "driver_id")
  private Driver driver;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  /** Legacy total (kept) */
  @Column(name = "total_amount", precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "delivery_address", nullable = false)
  private String deliveryAddress;

  @Column(name = "special_instructions")
  private String specialInstructions;

  @Builder.Default
  @Column(name = "age_verified")
  private boolean ageVerified = false;

  /** New: order type (DELIVERY/PICKUP) & promo code (MVP-lite) */
  @Column(name = "order_type")
  private String orderType; // "DELIVERY" | "PICKUP"

  @Column(name = "promo_code")
  private String promoCode;

  /** New: money breakdown (stored snapshots) */
  @Builder.Default
  @Column(name = "subtotal", precision = 10, scale = 2)
  private BigDecimal subtotal = BigDecimal.ZERO;

  @Builder.Default
  @Column(name = "tax", precision = 10, scale = 2)
  private BigDecimal tax = BigDecimal.ZERO;

  @Builder.Default
  @Column(name = "tip", precision = 10, scale = 2)
  private BigDecimal tip = BigDecimal.ZERO;

  @Builder.Default
  @Column(name = "fees", precision = 10, scale = 2)
  private BigDecimal fees = BigDecimal.ZERO;

  @Builder.Default
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default
  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Column(name = "estimated_delivery_time")
  private LocalDateTime estimatedDeliveryTime;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<OrderItem> items = new ArrayList<>();

  @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
  private Delivery delivery;

  @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
  private Payment payment;

  /* ---------- Domain helpers ---------- */

  public void addItem(OrderItem item) {
    item.setOrder(this);
    this.items.add(item);
  }

  public void removeItem(Long orderItemId) {
    this.items.removeIf(oi -> Objects.equals(oi.getId(), orderItemId));
  }

  /** Recalculate subtotal from items; leaves tax/tip/fees untouched. */
  public void recalcSubtotal() {
    this.subtotal = items.stream()
        .map(OrderItem::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /** Calculate full totals (subtotal + tax + tip + fees = total). */
  public void calculateTotals(BigDecimal taxRate /* e.g., 0.0200 */, BigDecimal tipOverride) {
    recalcSubtotal();
    // tax on subtotal only
    this.tax = subtotal.multiply(taxRate).setScale(2, BigDecimal.ROUND_HALF_UP);
    if (tipOverride != null)
      this.tip = tipOverride.setScale(2, BigDecimal.ROUND_HALF_UP);
    this.totalAmount = subtotal.add(tax).add(tip).add(fees).setScale(2, BigDecimal.ROUND_HALF_UP);
  }

  /** Apply a fixed-amount promo; never below zero. */
  public void applyPromo(BigDecimal discount) {
    if (discount == null || discount.signum() <= 0)
      return;
    recalcSubtotal();
    BigDecimal newSubtotal = subtotal.subtract(discount).max(BigDecimal.ZERO).setScale(2, BigDecimal.ROUND_HALF_UP);
    this.subtotal = newSubtotal;
  }

  public boolean canBeCancelled() {
    // Cancellable until we start preparing / pickup
    return status == OrderStatus.PENDING
        || status == OrderStatus.CONFIRMED
        || status == OrderStatus.CREATED
        || status == OrderStatus.AUTHORIZED
        || status == OrderStatus.ACCEPTED;
  }

  /** Minimal legal state transitions used in MVP */
  public boolean isValidStatusTransition(OrderStatus next) {
    Map<OrderStatus, List<OrderStatus>> allowed = Map.of(
        OrderStatus.CREATED, List.of(OrderStatus.AUTHORIZED, OrderStatus.CANCELLED),
        OrderStatus.AUTHORIZED, List.of(OrderStatus.ACCEPTED, OrderStatus.CANCELLED, OrderStatus.REJECTED),
        OrderStatus.ACCEPTED, List.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
        OrderStatus.PREPARING, List.of(OrderStatus.READY, OrderStatus.CANCELLED),
        OrderStatus.READY, List.of(OrderStatus.EN_ROUTE, OrderStatus.CANCELLED),
        OrderStatus.EN_ROUTE, List.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED),
        OrderStatus.DELIVERED, List.of(OrderStatus.REFUNDED) // optional post state
    );
    return allowed.getOrDefault(this.status, List.of()).contains(next);
  }

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}

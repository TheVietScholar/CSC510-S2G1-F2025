package com.boozebuddies.model;

/**
 * Represents the various stages of a delivery within the BoozeBuddies system. Each status indicates
 * the current progress or outcome of a delivery order.
 *
 * <p>Possible statuses include:
 *
 * <ul>
 *   <li>{@link #PENDING} - The order has been placed but not yet assigned to a delivery driver.
 *   <li>{@link #ASSIGNED} - The order has been assigned to a delivery driver.
 *   <li>{@link #PICKED_UP} - The delivery driver has picked up the order from the vendor.
 *   <li>{@link #IN_TRANSIT} - The order is currently being delivered to the customer.
 *   <li>{@link #DELIVERED} - The order has been successfully delivered to the customer.
 *   <li>{@link #FAILED} - The delivery attempt failed due to an issue (e.g., customer unavailable).
 *   <li>{@link #CANCELLED} - The order was cancelled before completion.
 * </ul>
 *
 * This enum can be used to track delivery progress, trigger notifications, or filter delivery
 * records based on their current state.
 */
public enum DeliveryStatus {
  /** The order has been placed but not yet assigned to a delivery driver. */
  PENDING,

  /** The order has been assigned to a delivery driver. */
  ASSIGNED,

  /** The delivery driver has picked up the order from the vendor. */
  PICKED_UP,

  /** The order is currently being delivered to the customer. */
  IN_TRANSIT,

  /** The order has been successfully delivered to the customer. */
  DELIVERED,

  /** The delivery attempt failed due to an issue (e.g., customer unavailable). */
  FAILED,

  /** The order was cancelled before completion. */
  CANCELLED
}

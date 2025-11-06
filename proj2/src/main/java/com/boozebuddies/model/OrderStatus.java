package com.boozebuddies.model;

/**
 * Represents the various stages of an order's lifecycle within the BoozeBuddies system. Each status
 * reflects the current progress or outcome of a customer's order, from initial placement to final
 * completion or cancellation.
 *
 * <p>Possible statuses include:
 *
 * <ul>
 *   <li>{@link #PENDING} - The order has been created but not yet confirmed.
 *   <li>{@link #CONFIRMED} - The order has been verified and accepted for processing.
 *   <li>{@link #PREPARING} - The vendor is currently preparing the items in the order.
 *   <li>{@link #READY_FOR_PICKUP} - The order is ready to be picked up by the delivery driver.
 *   <li>{@link #PICKED_UP} - The delivery driver has collected the order from the vendor.
 *   <li>{@link #IN_TRANSIT} - The order is currently being delivered to the customer.
 *   <li>{@link #DELIVERED} - The order has arrived at the customer's location.
 *   <li>{@link #COMPLETED} - The order process has been successfully finished and confirmed.
 *   <li>{@link #CANCELLED} - The order was cancelled before completion.
 *   <li>{@link #FAILED} - The order could not be completed due to an issue (e.g., payment or
 *       delivery failure).
 * </ul>
 *
 * This enum can be used to monitor order progress, trigger system events, or manage user
 * notifications throughout the order workflow.
 */
public enum OrderStatus {
  /** The order has been created but not yet confirmed. */
  PENDING,

  /** The order has been verified and accepted for processing. */
  CONFIRMED,

  /** The vendor is currently preparing the items in the order. */
  PREPARING,

  /** The order is ready to be picked up by the delivery driver. */
  READY_FOR_PICKUP,

  /** The delivery driver has collected the order from the vendor. */
  PICKED_UP,

  /** The order is currently being delivered to the customer. */
  IN_TRANSIT,

  /** The order has arrived at the customer's location. */
  DELIVERED,

  /** The order process has been successfully finished and confirmed. */
  COMPLETED,

  /** The order was cancelled before completion. */
  CANCELLED,

  /** The order could not be completed due to an issue (e.g., payment or delivery failure). */
  FAILED
}

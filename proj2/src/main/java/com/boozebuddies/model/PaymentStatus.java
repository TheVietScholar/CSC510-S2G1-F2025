package com.boozebuddies.model;

/**
 * Represents the various stages of a payment transaction within the BoozeBuddies system. Each
 * status indicates the current state or outcome of a payment made by a customer.
 *
 * <p>Possible statuses include:
 *
 * <ul>
 *   <li>{@link #PENDING} - The payment has been initiated but not yet processed or confirmed.
 *   <li>{@link #AUTHORIZED} - The payment has been approved and funds are reserved, but not yet
 *       captured.
 *   <li>{@link #CAPTURED} - The payment has been successfully completed and funds have been
 *       transferred.
 *   <li>{@link #FAILED} - The payment could not be completed due to an error or rejection.
 *   <li>{@link #REFUNDED} - The full amount has been refunded to the customer.
 *   <li>{@link #PARTIALLY_REFUNDED} - A portion of the payment has been refunded to the customer.
 * </ul>
 *
 * This enum can be used to track payment progress, handle refunds, and manage order or delivery
 * processing logic based on payment outcomes.
 */
public enum PaymentStatus {
  /** The payment has been initiated but not yet processed or confirmed. */
  PENDING,

  /** The payment has been approved and funds are reserved, but not yet captured. */
  AUTHORIZED,

  /** The payment has been successfully completed and funds have been transferred. */
  CAPTURED,

  /** The payment could not be completed due to an error or rejection. */
  FAILED,

  /** The full amount has been refunded to the customer. */
  REFUNDED,

  /** A portion of the payment has been refunded to the customer. */
  PARTIALLY_REFUNDED
}

package com.boozebuddies.model;

/**
 * Defines the different entities within the BoozeBuddies system that can receive user ratings. Each
 * value specifies the type of target being reviewed or rated by a customer.
 *
 * <p>Possible targets include:
 *
 * <ul>
 *   <li>{@link #MERCHANT} - Represents a rating given to a merchant or vendor.
 *   <li>{@link #DRIVER} - Represents a rating given to a delivery driver.
 *   <li>{@link #PRODUCT} - Represents a rating given to a specific product.
 * </ul>
 *
 * This enum is typically used to associate ratings or reviews with their corresponding entities,
 * enabling feedback tracking and performance analysis across the platform.
 */
public enum RatingTargetType {
  /** Represents a rating given to a merchant or vendor. */
  MERCHANT,

  /** Represents a rating given to a delivery driver. */
  DRIVER,

  /** Represents a rating given to a specific product. */
  PRODUCT
}

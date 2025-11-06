package com.boozebuddies.model;

/**
 * Represents the different user roles within the BoozeBuddies system. Each role defines a specific
 * set of permissions and responsibilities that determine what actions a user can perform on the
 * platform.
 *
 * <p>Available roles include:
 *
 * <ul>
 *   <li>{@link #USER} - A standard customer who can browsse and order products.
 *   <li>{@link #MERCHANT_ADMIN} - A merchant representative who manages store inventory, pricing,
 *       and orders.
 *   <li>{@link #DRIVER} - A delivery driver responsible for picking up and delivering customer
 *       orders.
 *   <li>{@link #ADMIN} - A system administrator with full access to manage users, merchants, and
 *       platform settings.
 * </ul>
 *
 * This enum is used for access control, authentication, and authorization throughout the
 * BoozeBuddies platform.
 */
public enum Role {
  /** A standard customer who can browse, order, and rate products. */
  USER,

  /** A merchant representative who manages store inventory, pricing, and orders. */
  MERCHANT_ADMIN,

  /** A delivery driver responsible for picking up and delivering customer orders. */
  DRIVER,

  /** A system administrator with full access to manage users, merchants, and platform settings. */
  ADMIN
}

//Need to figure out import and packages for the classes
package com.boozebuddies.delivery;

public enum DeliveryStatus {
  PENDING, // Order placed, waiting for driver assignment
  ASSIGNED, // Driver assigned but not started
  IN_TRANSIT, // Delivery in progress
  DELIVERED, // Delivery completed
  CANCELLED // Delivery cancelled
}

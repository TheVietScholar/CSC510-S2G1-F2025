package com.boozebuddies.model;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY_FOR_PICKUP,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    FAILED
}
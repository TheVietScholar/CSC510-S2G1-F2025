package com.boozebuddies.dto;

import java.util.List;

public class CreateOrderRequest {
    private Long userId;
    private Long merchantId;
    private String deliveryAddress;
    private List<OrderItemRequest> items;
    private String specialInstructions;

    // Constructors
    public CreateOrderRequest() {}
    
    public CreateOrderRequest(Long userId, Long merchantId, String deliveryAddress, List<OrderItemRequest> items) {
        this.userId = userId;
        this.merchantId = merchantId;
        this.deliveryAddress = deliveryAddress;
        this.items = items;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
}
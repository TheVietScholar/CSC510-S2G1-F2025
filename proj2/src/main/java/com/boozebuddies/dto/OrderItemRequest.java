package com.boozebuddies.dto;

import java.math.BigDecimal;

public class OrderItemRequest {
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;

    // Constructors
    public OrderItemRequest() {}
    
    public OrderItemRequest(Long productId, Integer quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
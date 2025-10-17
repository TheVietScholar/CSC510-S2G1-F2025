package com.boozebuddies.dto;

import java.math.BigDecimal;

public class CreateProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Long merchantId;
    private boolean isAlcohol;
    private Double alcoholContent;
    private Integer stockQuantity;
    private String imageUrl;

    // Constructors
    public CreateProductRequest() {}
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public boolean isAlcohol() { return isAlcohol; }
    public void setAlcohol(boolean alcohol) { isAlcohol = alcohol; }

    public Double getAlcoholContent() { return alcoholContent; }
    public void setAlcoholContent(Double alcoholContent) { this.alcoholContent = alcoholContent; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
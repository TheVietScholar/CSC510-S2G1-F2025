package com.boozebuddies.dto;

import java.math.BigDecimal;

public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Long merchantId;
    private String merchantName;
    private boolean isAlcohol;
    private Double alcoholContent;
    private Integer stockQuantity;
    private String imageUrl;
    private boolean available;

    // Constructors
    public ProductDTO() {}
    
    public ProductDTO(Long id, String name, String description, BigDecimal price, String category, Long merchantId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.merchantId = merchantId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public boolean isAlcohol() { return isAlcohol; }
    public void setAlcohol(boolean alcohol) { isAlcohol = alcohol; }

    public Double getAlcoholContent() { return alcoholContent; }
    public void setAlcoholContent(Double alcoholContent) { this.alcoholContent = alcoholContent; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
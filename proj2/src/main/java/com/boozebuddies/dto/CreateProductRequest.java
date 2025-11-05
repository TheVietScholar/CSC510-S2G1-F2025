package com.boozebuddies.dto;

import java.math.BigDecimal;
import lombok.*;

/**
 * Data transfer object for creating a new product.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {
  /** The name of the product */
  private String name;
  
  /** A description of the product */
  private String description;
  
  /** The price of the product */
  private BigDecimal price;
  
  /** The category the product belongs to */
  private String category;
  
  /** The ID of the merchant selling this product */
  private Long merchantId;
  
  /** Whether this product contains alcohol */
  private boolean isAlcohol;
  
  /** The alcohol content percentage (if applicable) */
  private Double alcoholContent;
  
  /** The number of units available in stock */
  private Integer stockQuantity;
  
  /** URL to the product image */
  private String imageUrl;
}
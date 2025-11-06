package com.boozebuddies.dto;

import java.math.BigDecimal;
import lombok.*;

/** Data transfer object for product information. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
  /** The unique product ID */
  private Long id;

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

  /** The name of the merchant selling this product */
  private String merchantName;

  /** Whether this product contains alcohol */
  private boolean isAlcohol;

  /** The alcohol content percentage (if applicable) */
  private Double alcoholContent;

  /** URL to the product image */
  private String imageUrl;

  /** Whether the product is currently available */
  private boolean isAvailable;

  /** The volume of the product in milliliters */
  private Integer volume;
}

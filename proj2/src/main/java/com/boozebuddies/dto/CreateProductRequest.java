package com.boozebuddies.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {
  private String name;
  private String description;
  private BigDecimal price;
  private String category;
  private Long merchantId;

  @JsonProperty("isAlcohol")  
  private boolean isAlcohol;

  private Double alcoholContent;

  @JsonProperty("isAvailable")  
  private boolean isAvailable;
  
  private String imageUrl;
}

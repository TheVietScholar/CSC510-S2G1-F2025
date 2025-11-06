package com.boozebuddies.dto;

import java.math.BigDecimal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
  private boolean isAvailable;
  private String imageUrl;
  private Integer volume;
}

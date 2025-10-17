package com.boozebuddies.dto;

import lombok.*;
import java.math.BigDecimal;

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
    private boolean isAlcohol;
    private Double alcoholContent;
    private Integer stockQuantity;
    private String imageUrl;
}
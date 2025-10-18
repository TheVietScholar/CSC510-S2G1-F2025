package com.boozebuddies.dto;

import java.time.LocalTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantDTO {
  private Long id;
  private String name;
  private String description;
  private String address;
  private String phone;
  private String email;
  private String cuisineType;
  private LocalTime openingTime;
  private LocalTime closingTime;
  private boolean isActive;
  private Double rating;
  private Integer totalRatings;
  private String imageUrl;
}

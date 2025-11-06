package com.boozebuddies.dto;

import java.time.LocalTime;
import lombok.*;

/** Data transfer object for merchant information. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantDTO {
  /** The unique merchant ID */
  private Long id;

  /** The merchant's name */
  private String name;

  /** A description of the merchant */
  private String description;

  /** The merchant's address */
  private String address;

  /** The merchant's phone number */
  private String phone;

  /** The merchant's email address */
  private String email;

  /** The type of cuisine offered by the merchant */
  private String cuisineType;

  /** The merchant's opening time */
  private LocalTime openingTime;

  /** The merchant's closing time */
  private LocalTime closingTime;

  /** Whether the merchant is currently active */
  private boolean isActive;

  /** The merchant's average rating */
  private Double rating;

  /** The total number of ratings received */
  private Integer totalRatings;

  /** URL to the merchant's image */
  private String imageUrl;

  /** The latitude coordinate of the merchant's location */
  private Double latitude;

  /** The longitude coordinate of the merchant's location */
  private Double longitude;
}

package com.boozebuddies.entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/** Entity representing a merchant that sells products. */
@Entity
@Table(name = "merchants")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Merchant {
  /** The unique merchant ID */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** The merchant's name */
  @Column(nullable = false)
  private String name;

  /** A description of the merchant */
  private String description;

  /** The merchant's address */
  @Column(nullable = false)
  private String address;

  /** The merchant's phone number */
  private String phone;

  /** The merchant's email address */
  private String email;

  /** The type of cuisine offered by the merchant */
  @Column(name = "cuisine_type")
  private String cuisineType;

  /** The merchant's opening time */
  @Column(name = "opening_time")
  private LocalTime openingTime;

  /** The merchant's closing time */
  @Column(name = "closing_time")
  private LocalTime closingTime;

  /** Whether the merchant is currently active */
  @Builder.Default
  @Column(name = "is_active")
  private boolean isActive = true;

  /** The merchant's average rating */
  @Builder.Default private Double rating = 0.0;

  /** The total number of ratings received */
  @Builder.Default
  @Column(name = "total_ratings")
  private Integer totalRatings = 0;

  /** URL to the merchant's image */
  @Column(name = "image_url")
  private String imageUrl;

  /** The latitude coordinate of the merchant's location */
  @Column(name = "latitude")
  private Double latitude;

  /** The longitude coordinate of the merchant's location */
  @Column(name = "longitude")
  private Double longitude;

  /** The list of products offered by this merchant */
  @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Product> products = new ArrayList<>();

  /** The list of orders placed with this merchant */
  @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Order> orders = new ArrayList<>();

  /** The list of ratings received by this merchant */
  @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Rating> ratings = new ArrayList<>();

  /**
   * Check if the merchant is currently open for business.
   *
   * @return true if the merchant is active and within operating hours, false otherwise
   */
  public boolean isOpen() {
    LocalTime now = LocalTime.now();
    return isActive && now.isAfter(openingTime) && now.isBefore(closingTime);
  }
}

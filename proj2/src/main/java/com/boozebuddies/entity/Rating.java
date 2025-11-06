package com.boozebuddies.entity;

import com.boozebuddies.model.RatingTargetType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/** Entity representing a rating and review for a merchant, driver, or product. */
@Entity
@Table(name = "ratings")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Rating {
  /** The unique rating ID */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** The user who submitted the rating */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /** The type of entity being rated */
  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false)
  private RatingTargetType targetType;

  /** The ID of the entity being rated */
  @Column(name = "target_id", nullable = false)
  private Long targetId;

  /** The rating value */
  @Column(nullable = false)
  private Integer rating;

  /** The text review accompanying the rating */
  private String review;

  /** When the rating was created */
  @Builder.Default
  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  // Optional relationships for easier querying
  /** The merchant being rated (if applicable) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "merchant_id")
  private Merchant merchant;

  /** The driver being rated (if applicable) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "driver_id")
  private Driver driver;

  /** The product being rated (if applicable) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;
}

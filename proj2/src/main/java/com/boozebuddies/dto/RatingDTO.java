package com.boozebuddies.dto;

import java.time.LocalDateTime;
import lombok.*;

/** Data transfer object for rating information. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingDTO {
  /** The unique rating ID */
  private Long id;

  /** The ID of the user who submitted the rating */
  private Long userId;

  /** The name of the user who submitted the rating */
  private String userName;

  /** The type of entity being rated (e.g., merchant, driver, product) */
  private String targetType;

  /** The ID of the entity being rated */
  private Long targetId;

  /** The rating value */
  private Integer rating;

  /** The text review accompanying the rating */
  private String review;

  /** When the rating was created */
  private LocalDateTime createdAt;
}

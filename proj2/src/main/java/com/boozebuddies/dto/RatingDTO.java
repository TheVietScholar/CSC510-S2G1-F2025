package com.boozebuddies.dto;

import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingDTO {
  private Long id;
  private Long userId;
  private String userName;
  private String targetType;
  private Long targetId;
  private Integer rating;
  private String comment;
  private LocalDateTime createdAt;
}

package com.boozebuddies.dto;

import lombok.*;
import java.time.LocalDateTime;

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
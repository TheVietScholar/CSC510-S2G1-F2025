package com.boozebuddies.mapper;

import com.boozebuddies.dto.RatingDTO;
import com.boozebuddies.entity.Rating;
import org.springframework.stereotype.Component;

/** Mapper for converting between Rating entities and RatingDTO objects. */
@Component
public class RatingMapper {

  /**
   * Converts a Rating entity to a RatingDTO.
   *
   * @param rating the rating entity to convert
   * @return the RatingDTO, or null if the input is null
   */
  public RatingDTO toDTO(Rating rating) {
    if (rating == null) return null;

    return RatingDTO.builder()
        .id(rating.getId())
        .userId(rating.getUser() != null ? rating.getUser().getId() : null)
        .userName(rating.getUser() != null ? rating.getUser().getName() : null)
        .targetType(rating.getTargetType().name())
        .targetId(rating.getTargetId())
        .rating(rating.getRating())
        .review(rating.getReview())
        .createdAt(rating.getCreatedAt())
        .build();
  }

  /**
   * Converts a RatingDTO to a Rating entity.
   *
   * @param ratingDTO the RatingDTO to convert
   * @return the Rating entity, or null if the input is null
   */
  public Rating toEntity(RatingDTO ratingDTO) {
    if (ratingDTO == null) return null;

    return Rating.builder()
        .rating(ratingDTO.getRating())
        .review(ratingDTO.getReview())
        .targetId(ratingDTO.getTargetId())
        .build();
  }
}

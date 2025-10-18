package com.boozebuddies.mapper;

import com.boozebuddies.dto.RatingDTO;
import com.boozebuddies.entity.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {

    public RatingDTO toDTO(Rating rating) {
        if (rating == null) return null;
        
        return RatingDTO.builder()
                .id(rating.getId())
                .userId(rating.getUser() != null ? rating.getUser().getId() : null)
                .userName(rating.getUser() != null ? rating.getUser().getName() : null)
                .targetType(rating.getTargetType().name())
                .targetId(rating.getTargetId())
                .rating(rating.getRating())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }

    public Rating toEntity(RatingDTO ratingDTO) {
        if (ratingDTO == null) return null;
        
        return Rating.builder()
                .rating(ratingDTO.getRating())
                .comment(ratingDTO.getComment())
                .targetId(ratingDTO.getTargetId())
                .build();
    }
}
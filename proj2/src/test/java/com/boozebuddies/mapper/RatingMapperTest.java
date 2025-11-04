package com.boozebuddies.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.boozebuddies.dto.RatingDTO;
import com.boozebuddies.entity.Rating;
import com.boozebuddies.entity.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RatingMapperTest {

  private RatingMapper ratingMapper;
  private Rating rating;
  private RatingDTO ratingDTO;

  @BeforeEach
  void setUp() {
    ratingMapper = new RatingMapper();

    User user = User.builder().id(1L).name("John Doe").build();

    rating =
        Rating.builder()
            .id(10L)
            .user(user)
            .targetId(100L)
            .rating(4)
            .review("Great product!")
            .createdAt(LocalDateTime.now())
            .build();

    ratingDTO =
        RatingDTO.builder()
            .id(10L)
            .userId(1L)
            .userName("John Doe")
            .targetId(100L)
            .rating(4)
            .review("Great product!")
            .createdAt(rating.getCreatedAt())
            .build();
  }

  @Test
  void testToEntity() {
    Rating entity = ratingMapper.toEntity(ratingDTO);

    assertNotNull(entity);
    assertEquals(ratingDTO.getRating(), entity.getRating());
    assertEquals(ratingDTO.getReview(), entity.getReview());
    assertEquals(ratingDTO.getTargetId(), entity.getTargetId());
    // User is not set in toEntity
    assertNull(entity.getUser());
  }

  @Test
  void testToDTO_NullInput() {
    assertNull(ratingMapper.toDTO(null));
  }

  @Test
  void testToEntity_NullInput() {
    assertNull(ratingMapper.toEntity(null));
  }
}

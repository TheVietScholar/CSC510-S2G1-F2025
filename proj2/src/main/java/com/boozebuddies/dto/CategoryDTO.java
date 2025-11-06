package com.boozebuddies.dto;

import lombok.*;

/** DTO for product category information. */
@Data
@AllArgsConstructor
@Builder
public class CategoryDTO {
  /** The category ID. */
  private Long id;

  /** The category name. */
  private String name;

  /** The category description. */
  private String description;

  /** The category image URL. */
  private String imageUrl;
}

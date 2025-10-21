package com.boozebuddies.dto;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
public class CategoryDTO {
  private Long id;
  private String name;
  private String description;
  private String imageUrl;
}

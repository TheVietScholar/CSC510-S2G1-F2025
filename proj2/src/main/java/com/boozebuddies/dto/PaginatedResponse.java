package com.boozebuddies.dto;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {
  private List<T> content;
  private int currentPage;
  private int totalPages;
  private long totalItems;
  private int pageSize;
  private boolean hasNext;
  private boolean hasPrevious;
}

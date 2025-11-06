package com.boozebuddies.dto;

import java.util.List;
import lombok.*;

/**
 * Generic data transfer object for paginated responses.
 *
 * @param <T> the type of content in the paginated response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {
  /** The list of items on the current page */
  private List<T> content;

  /** The current page number */
  private int currentPage;

  /** The total number of pages */
  private int totalPages;

  /** The total number of items across all pages */
  private long totalItems;

  /** The number of items per page */
  private int pageSize;

  /** Whether there is a next page */
  private boolean hasNext;

  /** Whether there is a previous page */
  private boolean hasPrevious;
}

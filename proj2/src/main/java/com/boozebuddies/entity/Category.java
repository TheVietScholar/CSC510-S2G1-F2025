package com.boozebuddies.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/** Entity representing a product category. */
@Entity
@Table(name = "categories")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Category {
  /** The unique category ID */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** The name of the category */
  @Column(nullable = false, unique = true)
  private String name;

  /** A description of the category */
  private String description;

  /** URL to the category image */
  @Column(name = "image_url")
  private String imageUrl;

  /** The list of products in this category */
  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Product> products = new ArrayList<>();
}

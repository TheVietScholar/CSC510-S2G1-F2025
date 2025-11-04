package com.boozebuddies.mapper;

import com.boozebuddies.dto.CreateProductRequest;
import com.boozebuddies.dto.ProductDTO;
import com.boozebuddies.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

  /** Convert a Product entity to a ProductDTO for API responses. */
  public ProductDTO toDTO(Product product) {
    if (product == null) return null;

    return ProductDTO.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .category(product.getCategory() != null ? product.getCategory().getName() : null)
        .merchantId(product.getMerchant() != null ? product.getMerchant().getId() : null)
        .merchantName(product.getMerchant() != null ? product.getMerchant().getName() : null)
        .isAlcohol(product.isAlcohol())
        .alcoholContent(product.getAlcoholContent())
        .imageUrl(product.getImageUrl())
        .available(product.isAvailable())
        .build();
  }

  /**
   * Convert a ProductDTO to a Product entity. (Relationships like category/merchant should be set
   * in the service layer.)
   */
  public Product toEntity(ProductDTO dto) {
    if (dto == null) return null;

    return Product.builder()
        .id(dto.getId())
        .name(dto.getName())
        .description(dto.getDescription())
        .price(dto.getPrice())
        .isAlcohol(dto.isAlcohol())
        .alcoholContent(dto.getAlcoholContent())
        .imageUrl(dto.getImageUrl())
        .available(dto.isAvailable())
        .build();
  }

  /**
   * Convert a CreateProductRequest to a Product entity. (Used for creating new products via API.)
   */
  public Product toEntity(CreateProductRequest request) {
    if (request == null) return null;

    return Product.builder()
        .name(request.getName())
        .description(request.getDescription())
        .price(request.getPrice())
        .isAlcohol(request.isAlcohol())
        .alcoholContent(request.getAlcoholContent())
        .imageUrl(request.getImageUrl())
        .available(true) // default to available for MVP
        .build();
  }
}

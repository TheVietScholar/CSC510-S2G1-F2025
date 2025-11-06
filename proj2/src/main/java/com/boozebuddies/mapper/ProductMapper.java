package com.boozebuddies.mapper;

import com.boozebuddies.dto.CreateProductRequest;
import com.boozebuddies.dto.ProductDTO;
import com.boozebuddies.entity.Product;
import org.springframework.stereotype.Component;

/** Mapper for converting between Product entities and Product-related DTO objects. */
@Component
public class ProductMapper {

  /**
   * Convert a Product entity to a ProductDTO for API responses.
   *
   * @param product the product entity to convert
   * @return the ProductDTO, or null if the input is null
   */
  public ProductDTO toDTO(Product product) {
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
        .isAvailable(product.isAvailable())
        .imageUrl(product.getImageUrl())
        .build();
  }

  /**
   * Convert a ProductDTO to a Product entity. Relationships like category/merchant should be set in
   * the service layer.
   *
   * @param dto the ProductDTO to convert
   * @return the Product entity, or null if the input is null
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
   * Convert a CreateProductRequest to a Product entity. Used for creating new products via API.
   *
   * @param request the CreateProductRequest to convert
   * @return the Product entity, or null if the input is null
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
        .available(request.isAvailable())
        .build();
  }
}

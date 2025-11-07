package com.boozebuddies.mapper;

import com.boozebuddies.dto.CreateProductRequest;
import com.boozebuddies.dto.ProductDTO;
import com.boozebuddies.entity.Category;
import com.boozebuddies.entity.Merchant;
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
    if (product == null) return null;

    return ProductDTO.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .categoryId(
            product.getCategory() != null
                ? product.getCategory().getId()
                : null) // Map category to categoryId
        .merchantId(product.getMerchant() != null ? product.getMerchant().getId() : null)
        .merchantName(product.getMerchant() != null ? product.getMerchant().getName() : null)
        .isAlcohol(product.isAlcohol())
        .alcoholContent(product.getAlcoholContent())
        .imageUrl(product.getImageUrl())
        .isAvailable(product.isAvailable())
        .volume(product.getVolume())
        .build();
  }

  /**
   * Convert a ProductDTO to a Product entity. Relationships like category/merchant should be set in
   * the service layer.
   *
   * @param dto the ProductDTO to convert
   * @return the Product entity, or null if the input is null
   */
  public Product toEntity(ProductDTO productDTO) {
    if (productDTO == null) return null;

    Product product = new Product();
    product.setId(productDTO.getId());
    product.setName(productDTO.getName());
    product.setDescription(productDTO.getDescription());
    product.setPrice(productDTO.getPrice());

    // Handle category - you'll need to fetch the Category entity by ID
    if (productDTO.getCategoryId() != null) {
      Category category = new Category();
      category.setId(productDTO.getCategoryId());
      product.setCategory(category);
    }

    // Handle merchant
    if (productDTO.getMerchantId() != null) {
      Merchant merchant = new Merchant();
      merchant.setId(productDTO.getMerchantId());
      product.setMerchant(merchant);
    }

    product.setAlcohol(productDTO.isAlcohol());
    product.setAlcoholContent(productDTO.getAlcoholContent());
    product.setImageUrl(productDTO.getImageUrl());
    product.setAvailable(productDTO.isAvailable());
    product.setVolume(productDTO.getVolume());

    return product;
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

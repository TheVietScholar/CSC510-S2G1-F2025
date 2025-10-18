package com.boozebuddies.mapper;

import com.boozebuddies.dto.CreateProductRequest;
import com.boozebuddies.dto.ProductDTO;
import com.boozebuddies.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

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
        .stockQuantity(product.getStockQuantity())
        .imageUrl(product.getImageUrl())
        .available(product.isAvailable())
        .build();
  }

  public Product toEntity(ProductDTO productDTO) {
    if (productDTO == null) return null;

    return Product.builder()
        .id(productDTO.getId())
        .name(productDTO.getName())
        .description(productDTO.getDescription())
        .price(productDTO.getPrice())
        .isAlcohol(productDTO.isAlcohol())
        .alcoholContent(productDTO.getAlcoholContent())
        .stockQuantity(productDTO.getStockQuantity())
        .imageUrl(productDTO.getImageUrl())
        .available(productDTO.isAvailable())
        .build();
  }

  public Product toEntity(CreateProductRequest request) {
    if (request == null) return null;

    return Product.builder()
        .name(request.getName())
        .description(request.getDescription())
        .price(request.getPrice())
        .isAlcohol(request.isAlcohol())
        .alcoholContent(request.getAlcoholContent())
        .stockQuantity(request.getStockQuantity())
        .imageUrl(request.getImageUrl())
        .available(true) // Default to available
        .build();
  }
}

package com.super_spring.product_service.utils.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.super_spring.product_service.entity.Product;
import com.super_spring.product_service.model.dto.InventoryDTO;
import com.super_spring.product_service.model.dto.ProductDTO;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "stock", ignore = true)
    ProductDTO toDto(Product product);

    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "active", source = "product.active")
    @Mapping(target = "stock", source = "inventory.stock")
    ProductDTO toDto(Product product, InventoryDTO inventory);

    Product toEntity(ProductDTO productDTO);

    List<ProductDTO> toDtoList(List<Product> products);
}

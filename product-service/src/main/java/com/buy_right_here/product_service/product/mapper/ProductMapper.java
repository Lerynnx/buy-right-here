package com.buy_right_here.product_service.product.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.buy_right_here.product_service.product.dto.ProductDTO;
import com.buy_right_here.product_service.inventory.dto.InventoryDTO;
import com.buy_right_here.product_service.product.entity.Product;

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

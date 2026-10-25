package com.super_spring.product_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.super_spring.product_service.client.InventoryApiClient;
import com.super_spring.product_service.entity.Product;
import com.super_spring.product_service.repository.IProductRepository;
import com.super_spring.product_service.model.dto.InventoryDTO;
import com.super_spring.product_service.model.dto.ProductDTO;
import com.super_spring.product_service.utils.mapper.ProductMapper;

@Service
public class ProductService {

    private final IProductRepository productRepository;
    private final InventoryApiClient inventoryApiClient;
    private final ProductMapper productMapper;

    public ProductService(
            IProductRepository productRepository,
            InventoryApiClient inventoryApiClient,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.inventoryApiClient = inventoryApiClient;
        this.productMapper = productMapper;
    }

    public List<ProductDTO> getProducts(int offset, int limit) {
        return productRepository.getProductsWithPagination(offset, limit).stream()
                .map(product -> productMapper.toDto(
                        product,
                        inventoryApiClient.getInventoryByProductId(product.getId())))
                .toList();
    }

    public int getTotalCount() {
        return (int) productRepository.count();
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            InventoryDTO inventory = inventoryApiClient.getInventoryByProductId(id);
            return productMapper.toDto(product, inventory);
        }
        return null;
    }

    //?Lo correcto seria llamar desde aqui al InventoryApiClient para crear o actualizar el inventario o que sea la gateway la que haga la llamada al InventoryApiClient?
    public String createOrUpdateProduct(Product product) {
        return Long.toString(productRepository.save(product).getId());
    }

    public boolean deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            return false;
        }
        productRepository.deleteById(id);
        inventoryApiClient.deleteInventory(id);
        return true;
    }

}
package com.super_spring.product_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.super_spring.product_service.entity.Product;
import com.super_spring.product_service.repository.IProductRepository;
import com.super_spring.product_service.model.dto.InventoryDTO;
import com.super_spring.product_service.model.dto.ProductDTO;
import com.super_spring.product_service.utils.mapper.ProductMapper;

import main.java.com.super_spring.product_service.client.InventoryApiClient;

@Service
public class ProductService {

    private final IProductRepository productRepository;
    private final InventoryApiClient inventoryApiClient;

    public ProductService(IProductRepository productRepository, InventoryApiClient inventoryApiClient) {
        this.productRepository = productRepository;
        this.inventoryApiClient = inventoryApiClient;
    }

    public List<ProductDTO> getProducts(int offset, int limit) {
        List<Product> products = productRepository.findAll(PageRequest.of(offset, limit)).getContent();
        List<ProductDTO> productDTOs = ProductMapper.toDTOList(products);

        for (ProductDTO productDTO : productDTOs) {
            InventoryDTO inventory = inventoryApiClient.getInventoryByProductId(productDTO.getId());
            if (inventory != null) {
                productDTO.setStock(inventory.getStock());
            }
        }

        return productDTOs;
    }

    public int getTotalCount() {
        return (int) productRepository.count();
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            InventoryDTO inventory = inventoryApiClient.getInventoryByProductId(id);
            if (inventory != null) {
                product.setStock(inventory.getStock());
            }
        }
        return ProductMapper.toDTO(product);
    }

    //?Lo correcto seria llamar desde aqui al InventoryApiClient para crear o actualizar el inventario o que sea la gateway la que haga la llamada al InventoryApiClient?
    public String createOrUpdateProduct(Product product) {
        return productRepository.save(product).getId().toString();
    }

    public boolean deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            return false;
        }
        productRepository.deleteById(id);
        inventoryApiClient.deleteInventoryByProductId(id);
        return true;
    }


}
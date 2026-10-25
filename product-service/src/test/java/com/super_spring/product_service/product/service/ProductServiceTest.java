package com.super_spring.product_service.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.super_spring.product_service.inventory.client.InventoryApiClient;
import com.super_spring.product_service.inventory.dto.InventoryDTO;
import com.super_spring.product_service.product.dto.ProductDTO;
import com.super_spring.product_service.product.entity.Product;
import com.super_spring.product_service.product.mapper.ProductMapper;
import com.super_spring.product_service.product.repository.ProductRepository;

class ProductServiceTest {

    private ProductRepository productRepository;
    private InventoryApiClient inventoryApiClient;
    private ProductMapper productMapper;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        inventoryApiClient = mock(InventoryApiClient.class);
        productMapper = mock(ProductMapper.class);
        productService = new ProductService(
                productRepository,
                inventoryApiClient,
                productMapper,
                new SimpleMeterRegistry());
    }

    @Test
    void getsProductWithInventoryOnlyWhenProductExists() {
        Product product = new Product();
        InventoryDTO inventory = new InventoryDTO(10L, 1L, 25, true);
        ProductDTO expected = mock(ProductDTO.class);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryApiClient.getInventoryByProductId(1L)).thenReturn(inventory);
        when(productMapper.toDto(product, inventory)).thenReturn(expected);

        assertEquals(expected, productService.getProductById(1L));

        verify(inventoryApiClient).getInventoryByProductId(1L);
        verify(productMapper).toDto(product, inventory);
    }

    @Test
    void returnsNullAndDoesNotCallInventoryWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(productService.getProductById(99L));

        verify(inventoryApiClient, never()).getInventoryByProductId(99L);
        verify(productMapper, never()).toDto(
                org.mockito.ArgumentMatchers.any(Product.class),
                org.mockito.ArgumentMatchers.any(InventoryDTO.class));
    }

    @Test
    void deletesProductAndItsInventoryWhenProductExists() {
        when(productRepository.existsById(1L)).thenReturn(true);

        assertEquals(true, productService.deleteProduct(1L));

        verify(productRepository).deleteById(1L);
        verify(inventoryApiClient).deleteInventory(1L);
    }

    @Test
    void doesNotDeleteInventoryWhenProductDoesNotExist() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertEquals(false, productService.deleteProduct(99L));

        verify(productRepository, never()).deleteById(99L);
        verify(inventoryApiClient, never()).deleteInventory(99L);
    }
}

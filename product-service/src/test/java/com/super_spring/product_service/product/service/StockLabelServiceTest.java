package com.super_spring.product_service.product.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.super_spring.product_service.product.entity.Product;
import com.super_spring.product_service.product.repository.ProductRepository;

class StockLabelServiceTest {

    private ProductRepository productRepository;
    private StockLabelService stockLabelService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        stockLabelService = new StockLabelService(productRepository);
    }

    @Test
    void persistsLabelReceivedFromInventoryUpdate() {
        Product product = new Product();
        product.setStockLabel("IN_STOCK");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        stockLabelService.updateLabel(1L, "LOW_STOCK");

        verify(productRepository).save(product);
    }
}

package com.super_spring.product_service.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

import com.super_spring.product_service.product.entity.Product;
import com.super_spring.product_service.product.repository.ProductRepository;

@Service
public class StockLabelService {

    private final ProductRepository productRepository;

    public StockLabelService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void updateLabel(long productId, String stockLabel) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return;
        }

        if (!Objects.equals(stockLabel, product.getStockLabel())) {
            product.setStockLabel(stockLabel);
            productRepository.save(product);
        }
    }
}

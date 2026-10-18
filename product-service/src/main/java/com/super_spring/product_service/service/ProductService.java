package com.super_spring.product_service.service;

import org.springframework.stereotype.Service;

import com.super_spring.product_service.entity.Product;
import com.super_spring.product_service.repository.IProductRepository;

@Service
public class ProductService {

    private final IProductRepository productRepository;

    public ProductService(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProducts(int offset, int limit) {
        return productRepository.findAll(PageRequest.of(offset, limit)).getContent();
    }

    public int getTotalCount() {
        return (int) productRepository.count();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public String createOrUpdateProduct(Product product) {
        return productRepository.save(product).getId().toString();
    }

    public boolean deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            return false;
        }
        productRepository.deleteById(id);
        return true;
    }
}
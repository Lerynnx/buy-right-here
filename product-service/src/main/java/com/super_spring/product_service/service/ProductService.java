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
        //TODO Implementation for fetching products with pagination
        return productRepository.getProductsWithPagination(offset, limit);
    }

    public int getTotalCount() {
        //TODO Implementation for fetching total count of products
        return 0;
    }

    public Product getProductById(Long id) {
        //TODO Implementation for fetching a product by its ID
        return 
    }

    public Product createOrUpdateProduct(Product product) {
        //TODO Implementation for creating or updating a product
        return new Product();
    }

    public void deleteProduct(Long id) {
        //TODO Implementation for deleting a product by its ID
    }

}
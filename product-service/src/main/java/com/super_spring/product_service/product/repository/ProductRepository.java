package com.super_spring.product_service.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.super_spring.product_service.config.ApplicationConstants;

import com.super_spring.product_service.product.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>  {

    @Query(value = ApplicationConstants.PRODUCT_PAGINATION_QUERY, nativeQuery = true)
    List<Product> getProductsWithPagination(@Param("offset") int offset, @Param("limit") int limit);
    
}
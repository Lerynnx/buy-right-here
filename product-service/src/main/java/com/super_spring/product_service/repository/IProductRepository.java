package com.super_spring.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface IProductRepository extends JpaRepository<Product, Integer>  {

    @Query(value = "SELECT * FROM products p LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Product> getProductsWithPagination(@Param("offset") int offset, @Param("limit") int limit);
    
}
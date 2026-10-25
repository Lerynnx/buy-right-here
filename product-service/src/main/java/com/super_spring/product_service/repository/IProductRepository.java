package com.super_spring.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.super_spring.product_service.entity.Product;
import java.util.List;


@Repository
public interface IProductRepository extends JpaRepository<Product, Long>  {

    @Query(value = "SELECT * FROM products p LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Product> getProductsWithPagination(@Param("offset") int offset, @Param("limit") int limit);
    
}
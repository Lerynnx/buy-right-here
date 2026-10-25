package com.super_spring.product_service.product.entity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
@Table(name="products")
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long categoryId;
    private String name;
    private String description;
    private double price;
    private double tax; //IVA percentage (e.g., 0.21 for 21% tax)
    private Instant uploadingDate;
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> imageUrls;
    private String brand;
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> attributes;
    private double rating;
    private int totalReviews;
    private boolean active;
}
package com.super_spring.product_service.entity;

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
    private List<String> imageUrls;
    private String brand;
    private Map<String, String> attributes;
    private double rating;
    private int totalReviews;
    private boolean active;
}
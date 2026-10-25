package com.super_spring.product_service.product.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.super_spring.product_service.product.dto.ProductDTO;
import com.super_spring.product_service.product.entity.Product;
import com.super_spring.product_service.product.mapper.ProductMapper;
import com.super_spring.product_service.product.service.ImportExportProductService;
import com.super_spring.product_service.product.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {
    
    private final ProductService productService;
    private final ImportExportProductService importExportProductService;
    private final ProductMapper productMapper;

    public ProductController(
            ProductService productService,
            ImportExportProductService importExportProductService,
            ProductMapper productMapper) {
        this.productService = productService;
        this.importExportProductService = importExportProductService;
        this.productMapper = productMapper;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProductsPaginated(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        if (offset < 0 || limit <= 0) {
            return ResponseEntity.badRequest().build();
        }else if (limit > 100) {
            limit = 100; // Cap the limit to 100 to prevent excessive data retrieval
        }
        List<ProductDTO> products = productService.getProducts(offset, limit);
        int totalCount = productService.getTotalCount();
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(totalCount)).body(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        ProductDTO product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<?> createOrUpdateProduct(@RequestBody ProductDTO productDTO) {
        if (productDTO == null) {
            return ResponseEntity.badRequest().body("Product must not be null");
        }
        Product product = productMapper.toEntity(productDTO);
        return ResponseEntity.ok(productService.createOrUpdateProduct(product));
    }

    @GetMapping("/export")
    public CompletableFuture<Void> exportProductsToCsv() {
        return importExportProductService.exportProductsToCsv();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/import")
    public ResponseEntity<?> importProductsFromCsv(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File must not be null or empty");
        }
        return ResponseEntity.accepted().body(importExportProductService.importProductsFromCsv(file));
    }
}
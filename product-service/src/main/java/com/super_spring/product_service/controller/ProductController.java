package com.super_spring.product_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.RestController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.super_spring.product_service.model.dto.ProductDTO;
import com.super_spring.product_service.service.ProductService;
import com.super_spring.product_service.service.ImportExportProductService;

@RestController
@RequestMapping("/products")
public class ProductController {
    
    private final ProductService productService;
    private final ImportExportProductService importExportProductService;

    public ProductController(ProductService productService, ImportExportProductService importExportProductService) {
        this.productService = productService;
        this.importExportProductService = importExportProductService;
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
    public String createOrUpdateProduct(@RequestBody Product product) {
        if (product == null || product.getId() == null) {
            return ResponseEntity.badRequest().body("Product or Product ID must not be null");
        }
        return productService.createOrUpdateProduct(product);
    }

    @GetMapping("/export")
    public String exportProductsToCsv() {
        return importExportProductService.exportProductsToCsv();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/import")
    public String importProductsFromCsv(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File must not be null or empty");
        }
        return importExportProductService.importProductsFromCsv(file);
    }
}
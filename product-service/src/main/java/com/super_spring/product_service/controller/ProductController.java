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

        List<ProductDTO> products = productService.getProducts(offset, limit);
        int totalCount = productService.getTotalCount();
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(totalCount)).body(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public String createOrUpdateProduct(@RequestBody Product product) {
        return productService.createOrUpdateProduct(product);
    }

    @GetMapping("/export")
    public MultipartFile exportProductsToCsv() {
        return importExportProductService.exportProductsToCsv();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/import")
    public String importProductsFromCsv(@RequestParam("file") MultipartFile file) {
        return importExportProductService.importProductsFromCsv(file);
    }
}
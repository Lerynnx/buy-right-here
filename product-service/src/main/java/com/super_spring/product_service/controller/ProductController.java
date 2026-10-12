package com.super_spring.product_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.RestController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.super_spring.product_service.model.dto.ProductDTO;
import com.super_spring.product_service.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {
    
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    //? It has to be DTO? Is the stock really necessary? Maybe it can be two methods, one for DTO and another for entity.
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProductsPaginated(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        List<ProductDTO> products = productService.getProducts(offset, limit);
        int totalCount = productService.getTotalCount();
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(totalCount)).body(products);
    }

    //? It has to be DTO? Is the stock really necessary? Maybe it can be two methods, one for DTO and another for entity.
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public String createOrUpdateProduct(@RequestBody Product product) {
        //TODO Validate user role by jwt token before allowing product creation or update
        return "result";
    }

    @GetMapping("/export")
    public MultipartFile exportProductsToCsv() {
        return "csv";
    }

    @PostMapping("/import")
    public String importProductsFromCsv(@RequestParam("file") MultipartFile file) {
        //TODO Validate user role by jwt token before allowing product creation or update
        return "result";
    }
}
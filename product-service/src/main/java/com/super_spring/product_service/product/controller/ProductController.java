package com.super_spring.product_service.product.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import com.super_spring.product_service.product.dto.ProductDTO;
import com.super_spring.product_service.product.dto.ExportStartResponse;
import com.super_spring.product_service.product.entity.Product;
import com.super_spring.product_service.product.mapper.ProductMapper;
import com.super_spring.product_service.product.service.ImportExportProductService;
import com.super_spring.product_service.product.service.ProductService;
import com.super_spring.product_service.config.ApplicationConstants;

@RestController
@RequestMapping(ApplicationConstants.PRODUCTS_PATH)
public class ProductController {
    
    private final ProductService productService;
    private final ImportExportProductService importExportProductService;
    private final ProductMapper productMapper;
    private final int maxLimit;

    public ProductController(
            ProductService productService,
            ImportExportProductService importExportProductService,
            ProductMapper productMapper,
            @Value("${app.pagination.max-limit}") int maxLimit) {
        this.productService = productService;
        this.importExportProductService = importExportProductService;
        this.productMapper = productMapper;
        this.maxLimit = maxLimit;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProductsPaginated(
            @RequestParam(defaultValue = ApplicationConstants.DEFAULT_OFFSET) int offset,
            @RequestParam(defaultValue = ApplicationConstants.DEFAULT_LIMIT) int limit) {
        if (offset < 0 || limit <= 0) {
            return ResponseEntity.badRequest().build();
        } else if (limit > maxLimit) {
            limit = maxLimit;
        }
        List<ProductDTO> products = productService.getProducts(offset, limit);
        int totalCount = productService.getTotalCount();
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().header(ApplicationConstants.TOTAL_COUNT_HEADER,
                String.valueOf(totalCount)).body(products);
    }

    @GetMapping(ApplicationConstants.PRODUCT_ID_PATH)
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

    @PreAuthorize("hasAnyRole('" + ApplicationConstants.ADMIN_ROLE + "', '" +
            ApplicationConstants.MANAGER_ROLE + "')")
    @PostMapping
    public ResponseEntity<?> createOrUpdateProduct(@RequestBody ProductDTO productDTO) {
        if (productDTO == null) {
            return ResponseEntity.badRequest().body(ApplicationConstants.PRODUCT_MUST_NOT_BE_NULL);
        }
        Product product = productMapper.toEntity(productDTO);
        return ResponseEntity.ok(productService.createOrUpdateProduct(product));
    }

    @GetMapping(ApplicationConstants.EXPORT_PATH)
    public ResponseEntity<ExportStartResponse> exportProductsToCsv(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.accepted().body(importExportProductService.startExport(jwt.getSubject()));
    }

    @GetMapping(ApplicationConstants.EXPORT_FILE_PATH)
    public ResponseEntity<Resource> downloadExport(
            @PathVariable String fileName,
            @AuthenticationPrincipal Jwt jwt) {
        return importExportProductService.download(fileName, jwt.getSubject());
    }

    @PreAuthorize("hasAnyRole('" + ApplicationConstants.ADMIN_ROLE + "', '" +
            ApplicationConstants.MANAGER_ROLE + "')")
    @PostMapping(ApplicationConstants.IMPORT_PATH)
    public ResponseEntity<?> importProductsFromCsv(
            @RequestParam(ApplicationConstants.FILE_REQUEST_PARAM) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApplicationConstants.FILE_MUST_NOT_BE_NULL_OR_EMPTY);
        }
        return ResponseEntity.accepted().body(importExportProductService.importProductsFromCsv(file));
    }
}
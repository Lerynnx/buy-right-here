package com.buy_right_here.product_service.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.MeterRegistry;

import com.buy_right_here.product_service.inventory.client.InventoryApiClient;
import com.buy_right_here.product_service.product.dto.ProductDTO;
import com.buy_right_here.product_service.inventory.dto.InventoryDTO;
import com.buy_right_here.product_service.product.entity.Product;
import com.buy_right_here.product_service.product.mapper.ProductMapper;
import com.buy_right_here.product_service.product.repository.ProductRepository;
import com.buy_right_here.product_service.config.ApplicationConstants;

@Service
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final InventoryApiClient inventoryApiClient;
    private final ProductMapper productMapper;
    private final MeterRegistry meterRegistry;

    public ProductService(
            ProductRepository productRepository,
            InventoryApiClient inventoryApiClient,
            ProductMapper productMapper,
            MeterRegistry meterRegistry) {
        this.productRepository = productRepository;
        this.inventoryApiClient = inventoryApiClient;
        this.productMapper = productMapper;
        this.meterRegistry = meterRegistry;
    }

    public List<ProductDTO> getProducts(int offset, int limit) {
        List<ProductDTO> products = productRepository.getProductsWithPagination(offset, limit).stream()
                .map(productMapper::toDto)
                .toList();
        recordOperation("list", true);
        LOGGER.debug("Listed {} products with offset {} and limit {}", products.size(), offset, limit);
        return products;
    }

    public int getTotalCount() {
        return (int) productRepository.count();
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            InventoryDTO inventory = inventoryApiClient.getInventoryByProductId(id);
            recordOperation("get", true);
            return productMapper.toDto(product, inventory);
        }
        recordOperation("get", false);
        LOGGER.debug("Product {} was not found", id);
        return null;
    }

    //?Lo correcto seria llamar desde aqui al InventoryApiClient para crear o actualizar el inventario o que sea la gateway la que haga la llamada al InventoryApiClient?
    public String createOrUpdateProduct(Product product) {
        Product saved = productRepository.save(product);
        recordOperation("create_or_update", true);
        LOGGER.info("Product {} was created or updated", saved.getId());
        return Long.toString(saved.getId());
    }

    public boolean deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            recordOperation("delete", false);
            LOGGER.debug("Product {} was not found for deletion", id);
            return false;
        }
        productRepository.deleteById(id);
        inventoryApiClient.deleteInventory(id);
        recordOperation("delete", true);
        LOGGER.info("Product {} was deleted", id);
        return true;
    }

    private void recordOperation(String operation, boolean success) {
        meterRegistry.counter(ApplicationConstants.PRODUCT_OPERATIONS_METRIC,
                ApplicationConstants.METRIC_OPERATION_TAG, operation,
                ApplicationConstants.RESULT_TAG,
                success ? ApplicationConstants.SUCCESS_RESULT : ApplicationConstants.FAILURE_RESULT)
                .increment();
    }

}
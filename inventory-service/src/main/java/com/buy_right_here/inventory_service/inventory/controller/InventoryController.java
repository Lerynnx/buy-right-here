package com.buy_right_here.inventory_service.inventory.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.buy_right_here.inventory_service.config.ApplicationConstants;
import com.buy_right_here.inventory_service.inventory.dto.InventoryDTO;
import com.buy_right_here.inventory_service.inventory.service.InventoryService;

@RestController
@RequestMapping(ApplicationConstants.INVENTORY_PATH)
public class InventoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryController.class);
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping(ApplicationConstants.INVENTORY_PRODUCT_PATH)
    public ResponseEntity<InventoryDTO> getByProductId(@PathVariable long productId) {
        if (productId <= 0) {
            LOGGER.debug("Rejected inventory lookup with invalid product id {}", productId);
            return ResponseEntity.badRequest().build();
        }
        InventoryDTO inventory = inventoryService.getByProductId(productId);
        return inventory == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(inventory);
    }

    @GetMapping
    public ResponseEntity<List<InventoryDTO>> getInventories(
            @RequestParam(defaultValue = ApplicationConstants.DEFAULT_OFFSET) int offset,
            @RequestParam(defaultValue = ApplicationConstants.DEFAULT_LIMIT) int limit) {
        if (offset < 0 || limit <= 0) {
            LOGGER.debug("Rejected inventory list with invalid pagination offset {} and limit {}", offset, limit);
            return ResponseEntity.badRequest().build();
        }
        List<InventoryDTO> inventories = inventoryService.getAll(offset, limit);
        return inventories.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(inventories);
    }

    @PostMapping
    public ResponseEntity<InventoryDTO> create(@RequestBody InventoryDTO inventory) {
        if (inventory == null || inventory.productId() <= 0 || inventory.stock() < 0) {
            LOGGER.debug("Rejected invalid inventory creation request");
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(201).body(inventoryService.create(inventory));
    }

    @PutMapping(ApplicationConstants.INVENTORY_PRODUCT_PATH)
    public ResponseEntity<InventoryDTO> update(
            @PathVariable long productId,
            @RequestBody InventoryDTO inventory) {
        if (productId <= 0 || inventory == null || inventory.stock() < 0) {
            LOGGER.debug("Rejected invalid inventory update request for product {}", productId);
            return ResponseEntity.badRequest().build();
        }
        InventoryDTO updated = inventoryService.update(productId, inventory);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @PostMapping(ApplicationConstants.INVENTORY_BATCH_PATH)
    public ResponseEntity<Void> createOrUpdateBatch(@RequestBody List<InventoryDTO> inventories) {
        if (inventories == null || inventories.stream().anyMatch(inventory ->
                inventory == null || inventory.productId() <= 0 || inventory.stock() < 0)) {
            LOGGER.debug("Rejected invalid inventory batch request");
            return ResponseEntity.badRequest().build();
        }
        inventoryService.createOrUpdateBatch(inventories);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(ApplicationConstants.INVENTORY_PRODUCT_PATH)
    public ResponseEntity<Void> delete(@PathVariable long productId) {
        if (productId <= 0) {
            LOGGER.debug("Rejected inventory deletion with invalid product id {}", productId);
            return ResponseEntity.badRequest().build();
        }
        return inventoryService.deleteLogically(productId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}

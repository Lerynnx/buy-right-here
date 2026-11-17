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

import reactor.core.publisher.Mono;

@RestController
@RequestMapping(ApplicationConstants.INVENTORY_PATH)
public class InventoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryController.class);
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping(ApplicationConstants.INVENTORY_PRODUCT_SUBPATH)
    public Mono<ResponseEntity<InventoryDTO>> getByProductId(@PathVariable long productId) {
        if (productId <= 0) {
            LOGGER.debug("Rejected inventory lookup with invalid product id {}", productId);
            return Mono.just(ResponseEntity.<InventoryDTO>badRequest().build());
        }
        return inventoryService.getByProductId(productId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.<InventoryDTO>notFound().build());
    }

    @GetMapping
    public Mono<ResponseEntity<List<InventoryDTO>>> getInventories(
            @RequestParam(defaultValue = ApplicationConstants.DEFAULT_OFFSET) int offset,
            @RequestParam(defaultValue = ApplicationConstants.DEFAULT_LIMIT) int limit) {
        if (offset < 0 || limit <= 0) {
            LOGGER.debug("Rejected inventory list with invalid pagination offset {} and limit {}", offset, limit);
            return Mono.just(ResponseEntity.<List<InventoryDTO>>badRequest().build());
        }
        return inventoryService.getAll(offset, limit)
                .collectList()
                .map(inventories -> inventories.isEmpty()
                        ? ResponseEntity.<List<InventoryDTO>>noContent().build()
                        : ResponseEntity.ok(inventories));
    }

    @PostMapping
    public Mono<ResponseEntity<InventoryDTO>> create(@RequestBody InventoryDTO inventory) {
        if (inventory == null || inventory.productId() <= 0 || inventory.stock() < 0) {
            LOGGER.debug("Rejected invalid inventory creation request");
            return Mono.just(ResponseEntity.<InventoryDTO>badRequest().build());
        }
        return inventoryService.create(inventory)
                .map(created -> ResponseEntity.status(201).body(created));
    }

    @PutMapping(ApplicationConstants.INVENTORY_PRODUCT_SUBPATH)
    public Mono<ResponseEntity<InventoryDTO>> update(
            @PathVariable long productId,
            @RequestBody InventoryDTO inventory) {
        if (productId <= 0 || inventory == null || inventory.stock() < 0) {
            LOGGER.debug("Rejected invalid inventory update request for product {}", productId);
            return Mono.just(ResponseEntity.<InventoryDTO>badRequest().build());
        }
        return inventoryService.update(productId, inventory)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.<InventoryDTO>notFound().build());
    }

    @PostMapping(ApplicationConstants.INVENTORY_BATCH_SUBPATH)
    public Mono<ResponseEntity<Void>> createOrUpdateBatch(@RequestBody List<InventoryDTO> inventories) {
        if (inventories == null || inventories.stream().anyMatch(inventory ->
                inventory == null || inventory.productId() <= 0 || inventory.stock() < 0)) {
            LOGGER.debug("Rejected invalid inventory batch request");
            return Mono.just(ResponseEntity.<Void>badRequest().build());
        }
        return inventoryService.createOrUpdateBatch(inventories)
                .thenReturn(ResponseEntity.<Void>noContent().build());
    }

    @DeleteMapping(ApplicationConstants.INVENTORY_PRODUCT_SUBPATH)
    public Mono<ResponseEntity<Void>> delete(@PathVariable long productId) {
        if (productId <= 0) {
            LOGGER.debug("Rejected inventory deletion with invalid product id {}", productId);
            return Mono.just(ResponseEntity.<Void>badRequest().build());
        }
        return inventoryService.deleteLogically(productId)
                .map(deleted -> deleted
                        ? ResponseEntity.<Void>noContent().build()
                        : ResponseEntity.<Void>notFound().build());
    }
}

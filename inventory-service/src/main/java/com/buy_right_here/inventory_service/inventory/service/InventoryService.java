package com.buy_right_here.inventory_service.inventory.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buy_right_here.inventory_service.inventory.dto.InventoryDTO;
import com.buy_right_here.inventory_service.inventory.entity.Inventory;
import com.buy_right_here.inventory_service.mapper.InventoryMapper;
import com.buy_right_here.inventory_service.messaging.InventoryLabelPublisher;
import com.buy_right_here.inventory_service.repository.InventoryRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class InventoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryService.class);
    private static final int LAST_UNITS_THRESHOLD = 5;
    private static final int LOW_STOCK_THRESHOLD = 10;

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final InventoryLabelPublisher labelPublisher;

    public InventoryService(
            InventoryRepository inventoryRepository,
            InventoryMapper inventoryMapper,
            InventoryLabelPublisher labelPublisher) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMapper = inventoryMapper;
        this.labelPublisher = labelPublisher;
    }

    public Mono<InventoryDTO> getByProductId(long productId) {
        return inventoryRepository.findByProductIdAndActiveTrue(productId)
                .map(inventoryMapper::toDto)
                .doOnNext(inventory -> LOGGER.debug("Retrieved inventory for product {}", productId))
                .switchIfEmpty(Mono.defer(() -> {
                    LOGGER.debug("Inventory for product {} was not found", productId);
                    return Mono.<InventoryDTO>empty();
                }));
    }

    public Flux<InventoryDTO> getAll(int offset, int limit) {
        return inventoryRepository.findAllByActiveTrueOrderByProductIdAsc()
                .skip(offset)
                .take(limit)
                .map(inventoryMapper::toDto)
                .doOnComplete(() ->
                        LOGGER.debug("Listed inventories with offset {} and limit {}", offset, limit));
    }

    @Transactional
    public Mono<InventoryDTO> create(InventoryDTO dto) {
        Inventory inventory = inventoryMapper.toEntity(dto);
        inventory.setId(null);
        inventory.setActive(true);
        return inventoryRepository.save(inventory)
                .doOnNext(saved -> {
                    publishLabelIfChanged(null, saved);
                    LOGGER.info("Inventory for product {} was created", saved.getProductId());
                })
                .map(inventoryMapper::toDto);
    }

    @Transactional
    public Mono<InventoryDTO> update(long productId, InventoryDTO dto) {
        return inventoryRepository.findByProductIdAndActiveTrue(productId)
                .flatMap(current -> {
                    String oldLabel = stockLabel(current.getStock());
                    current.setStock(dto.stock());
                    current.setActive(true);
                    return inventoryRepository.save(current)
                            .doOnNext(saved -> {
                                publishLabelIfChanged(oldLabel, saved);
                                LOGGER.info("Inventory for product {} was updated", productId);
                            })
                            .map(inventoryMapper::toDto);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    LOGGER.debug("Inventory for product {} was not found for update", productId);
                    return Mono.<InventoryDTO>empty();
                }));
    }

    @Transactional
    public Mono<Void> createOrUpdateBatch(List<InventoryDTO> dtos) {
        LOGGER.debug("Processing inventory batch with {} records", dtos.size());
        return Flux.fromIterable(dtos)
                .concatMap(dto -> inventoryRepository.findByProductId(dto.productId())
                        .flatMap(current -> {
                            String oldLabel = current.isActive() ? stockLabel(current.getStock()) : null;
                            current.setStock(dto.stock());
                            current.setActive(true);
                            return inventoryRepository.save(current)
                                    .doOnNext(saved -> publishLabelIfChanged(oldLabel, saved))
                                    .then();
                        })
                        .switchIfEmpty(create(dto).then()))
                .then();
    }

    @Transactional
    public Mono<Boolean> deleteLogically(long productId) {
        return inventoryRepository.findByProductIdAndActiveTrue(productId)
                .flatMap(current -> {
                    current.setActive(false);
                    return inventoryRepository.save(current)
                            .doOnNext(saved -> LOGGER.info("Inventory for product {} was deleted", productId))
                            .thenReturn(true);
                })
                .defaultIfEmpty(false)
                .doOnNext(deleted -> {
                    if (!deleted) {
                        LOGGER.debug("Inventory for product {} was not found for deletion", productId);
                    }
                });
    }

    private void publishLabelIfChanged(String oldLabel, Inventory inventory) {
        String newLabel = stockLabel(inventory.getStock());
        if (!newLabel.equals(oldLabel)) {
            LOGGER.debug("Stock label for product {} changed from {} to {}",
                    inventory.getProductId(), oldLabel, newLabel);
            labelPublisher.publish(inventory.getProductId(), newLabel);
        }
    }

    private String stockLabel(int stock) {
        if (stock <= 0) {
            return "Sold Out";
        }
        if (stock <= LAST_UNITS_THRESHOLD) {
            return "Last Units";
        }
        if (stock <= LOW_STOCK_THRESHOLD) {
            return "Low Stock";
        }
        return "Stock";
    }
}

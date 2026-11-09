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

    public InventoryDTO getByProductId(long productId) {
        InventoryDTO inventory = inventoryRepository.findByProductIdAndActiveTrue(productId)
                .map(inventoryMapper::toDto)
                .block();
        if (inventory == null) {
            LOGGER.debug("Inventory for product {} was not found", productId);
        } else {
            LOGGER.debug("Retrieved inventory for product {}", productId);
        }
        return inventory;
    }

    public List<InventoryDTO> getAll(int offset, int limit) {
        List<InventoryDTO> inventories = inventoryRepository.findAllByActiveTrueOrderByProductIdAsc()
                .skip(offset)
                .take(limit)
                .map(inventoryMapper::toDto)
                .collectList()
                .block();
        LOGGER.debug("Listed {} inventories with offset {} and limit {}", inventories.size(), offset, limit);
        return inventories;
    }

    @Transactional
    public InventoryDTO create(InventoryDTO dto) {
        Inventory inventory = inventoryMapper.toEntity(dto);
        inventory.setId(null);
        inventory.setActive(true);
        Inventory saved = inventoryRepository.save(inventory).block();
        publishLabelIfChanged(null, saved);
        LOGGER.info("Inventory for product {} was created", saved.getProductId());
        return inventoryMapper.toDto(saved);
    }

    @Transactional
    public InventoryDTO update(long productId, InventoryDTO dto) {
        Inventory current = inventoryRepository.findByProductIdAndActiveTrue(productId).block();
        if (current == null) {
            LOGGER.debug("Inventory for product {} was not found for update", productId);
            return null;
        }

        String oldLabel = stockLabel(current.getStock());
        current.setStock(dto.stock());
        current.setActive(true);
        Inventory saved = inventoryRepository.save(current).block();
        publishLabelIfChanged(oldLabel, saved);
        LOGGER.info("Inventory for product {} was updated", productId);
        return inventoryMapper.toDto(saved);
    }

    @Transactional
    public void createOrUpdateBatch(List<InventoryDTO> dtos) {
        LOGGER.debug("Processing inventory batch with {} records", dtos.size());
        for (InventoryDTO dto : dtos) {
            Inventory current = inventoryRepository.findByProductId(dto.productId()).block();
            if (current == null) {
                create(dto);
            } else {
                String oldLabel = current.isActive() ? stockLabel(current.getStock()) : null;
                current.setStock(dto.stock());
                current.setActive(true);
                Inventory saved = inventoryRepository.save(current).block();
                publishLabelIfChanged(oldLabel, saved);
            }
        }
    }

    @Transactional
    public boolean deleteLogically(long productId) {
        Inventory current = inventoryRepository.findByProductIdAndActiveTrue(productId).block();
        if (current == null) {
            LOGGER.debug("Inventory for product {} was not found for deletion", productId);
            return false;
        }
        current.setActive(false);
        inventoryRepository.save(current).block();
        LOGGER.info("Inventory for product {} was deleted", productId);
        return true;
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

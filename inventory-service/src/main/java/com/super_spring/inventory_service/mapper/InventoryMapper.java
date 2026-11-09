package com.super_spring.inventory_service.mapper;

import org.springframework.stereotype.Component;

import com.super_spring.inventory_service.inventory.dto.InventoryDTO;
import com.super_spring.inventory_service.inventory.entity.Inventory;

@Component
public class InventoryMapper {

    public InventoryDTO toDto(Inventory inventory) {
        return new InventoryDTO(
                inventory.getId() == null ? 0L : inventory.getId(),
                inventory.getProductId(),
                inventory.getStock(),
                inventory.isActive());
    }

    public Inventory toEntity(InventoryDTO dto) {
        return new Inventory(dto.id() == 0L ? null : dto.id(), dto.productId(), dto.stock(), dto.isActive());
    }
}

package com.super_spring.inventory_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.super_spring.inventory_service.inventory.dto.InventoryDTO;
import com.super_spring.inventory_service.inventory.entity.Inventory;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "nullableId")
    @Mapping(target = "isActive", source = "active")
    InventoryDTO toDto(Inventory inventory);

    @Mapping(target = "id", source = "id", qualifiedByName = "zeroAsNull")
    @Mapping(target = "active", source = "isActive")
    Inventory toEntity(InventoryDTO dto);

    @Named("nullableId")
    default long nullableId(Long id) {
        return id == null ? 0L : id;
    }

    @Named("zeroAsNull")
    default Long zeroAsNull(long id) {
        return id == 0L ? null : id;
    }
}

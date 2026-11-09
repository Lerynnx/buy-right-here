package com.super_spring.inventory_service.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.super_spring.inventory_service.inventory.entity.Inventory;

public interface InventoryRepository extends R2dbcRepository<Inventory, Long> {

    Mono<Inventory> findByProductIdAndActiveTrue(long productId);

    Flux<Inventory> findAllByActiveTrueOrderByProductIdAsc();

    Mono<Inventory> findByProductId(long productId);
}

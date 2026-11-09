package com.buy_right_here.inventory_service.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.buy_right_here.inventory_service.inventory.entity.Inventory;

public interface InventoryRepository extends R2dbcRepository<Inventory, Long> {

    Mono<Inventory> findByProductIdAndActiveTrue(long productId);

    Flux<Inventory> findAllByActiveTrueOrderByProductIdAsc();

    Mono<Inventory> findByProductId(long productId);
}

package com.buy_right_here.inventory_service.inventory.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data @AllArgsConstructor @NoArgsConstructor
@Table("inventories")
public class Inventory {
    @Id
    private Long id;
    @Column("product_id")
    private long productId;
    private int stock;
    @Column("is_active")
    private boolean active;
}

package com.super_spring.inventory_service.inventory.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class Inventory {
    //! I DON'T HAVE TRADITIONAL JAKARTA PERSISTENCE ANNOTATIONS BECAUSE I AM USING A DATA R2DBC
    private long id;
    private long productId;
    private int stock;
    private boolean isActive;
}

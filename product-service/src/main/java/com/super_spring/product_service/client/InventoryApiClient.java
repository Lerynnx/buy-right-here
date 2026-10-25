package com.super_spring.product_service.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.super_spring.product_service.model.dto.InventoryDTO;

@Component
public class InventoryApiClient {

    private final RestClient restClient;

    public InventoryApiClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:8085/")
                .build();
    }

    public InventoryDTO getInventoryByProductId(Long productId) {

        return restClient.get()
                .uri("/inventory/{productId}", productId)
                .retrieve()
                .body(new ParameterizedTypeReference<InventoryDTO>() {});
    }

    public List<InventoryDTO> getAllInventories() {
        return restClient.get()
                .uri("/inventory")
                .retrieve()
                .body(new ParameterizedTypeReference<List<InventoryDTO>>() {});
    }

    public InventoryDTO createInventory(InventoryDTO inventoryDTO) {
        return restClient.post()
                .uri("/inventory")
                .body(inventoryDTO)
                .retrieve()
                .body(new ParameterizedTypeReference<InventoryDTO>() {});
    }

    public InventoryDTO updateInventory(Long productId, InventoryDTO inventoryDTO) {
        return restClient.put()
                .uri("/inventory/{productId}", productId)
                .body(inventoryDTO)
                .retrieve()
                .body(new ParameterizedTypeReference<InventoryDTO>() {});
    }

    //For cascading delete when a product is deleted, we can call this method to delete the corresponding inventory.
    public void deleteInventory(Long productId) {
        restClient.delete()
                .uri("/inventory/{productId}", productId)
                .retrieve()
                .toBodilessEntity();
    }
}
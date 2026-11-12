package com.buy_right_here.product_service.inventory.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;

import com.buy_right_here.product_service.inventory.dto.InventoryDTO;
import com.buy_right_here.product_service.config.ApplicationConstants;

@Component
public class InventoryApiClient {

    private final RestClient restClient;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public InventoryApiClient(
            RestClient.Builder builder,
            OAuth2AuthorizedClientManager authorizedClientManager,
            @org.springframework.beans.factory.annotation.Value("${app.inventory.base-url}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
        this.authorizedClientManager = authorizedClientManager;
    }

    public InventoryDTO getInventoryByProductId(Long productId) {

        return restClient.get()
                .uri(ApplicationConstants.INVENTORY_PRODUCT_PATH, productId)
                .retrieve()
                .body(new ParameterizedTypeReference<InventoryDTO>() {});
    }

    public List<InventoryDTO> getAllInventories() {
        return restClient.get()
                .uri(ApplicationConstants.INVENTORY_PATH)
                .retrieve()
                .body(new ParameterizedTypeReference<List<InventoryDTO>>() {});
    }

    public List<InventoryDTO> getInventories(int offset, int limit) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApplicationConstants.INVENTORY_PATH)
                        .queryParam("offset", offset)
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<InventoryDTO>>() {});
    }

    public InventoryDTO createInventory(InventoryDTO inventoryDTO) {
        return restClient.post()
                .uri(ApplicationConstants.INVENTORY_PATH)
                .body(inventoryDTO)
                .retrieve()
                .body(new ParameterizedTypeReference<InventoryDTO>() {});
    }

    public void createOrUpdateInventories(List<InventoryDTO> inventories, String batchPath) {
        var authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("inventory-service")
                .principal("product-service")
                .build();
        var authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new IllegalStateException("Unable to obtain service token for inventory-service");
        }
        restClient.post()
                .uri(batchPath)
                .headers(headers -> headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue()))
                .body(inventories)
                .retrieve()
                .toBodilessEntity();
    }

    public InventoryDTO updateInventory(Long productId, InventoryDTO inventoryDTO) {
        return restClient.put()
                .uri(ApplicationConstants.INVENTORY_PRODUCT_PATH, productId)
                .body(inventoryDTO)
                .retrieve()
                .body(new ParameterizedTypeReference<InventoryDTO>() {});
    }

    //For cascading delete when a product is deleted, we can call this method to delete the corresponding inventory.
    public void deleteInventory(Long productId) {
        restClient.delete()
                .uri(ApplicationConstants.INVENTORY_PRODUCT_PATH, productId)
                .retrieve()
                .toBodilessEntity();
    }
}
package com.buy_right_here.inventory_service.inventory.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;

import com.buy_right_here.inventory_service.inventory.dto.InventoryDTO;
import com.buy_right_here.inventory_service.inventory.service.InventoryService;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    private InventoryController controller;

    @BeforeEach
    void setUp() {
        controller = new InventoryController(inventoryService);
    }

    @Test
    void rejectsInvalidProductId() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.getByProductId(0).getStatusCode());

        verifyNoInteractions(inventoryService);
    }

    @Test
    void returnsNoContentWhenInventoryListIsEmpty() {
        when(inventoryService.getAll(0, 10)).thenReturn(List.of());

        assertEquals(HttpStatus.NO_CONTENT, controller.getInventories(0, 10).getStatusCode());
    }

    @Test
    void createsValidInventory() {
        InventoryDTO input = new InventoryDTO(0, 7, 12, true);
        when(inventoryService.create(input)).thenReturn(new InventoryDTO(1, 7, 12, true));

        var response = controller.create(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().id());
        verify(inventoryService).create(input);
    }

    @Test
    void rejectsInvalidBatch() {
        var response = controller.createOrUpdateBatch(List.of(new InventoryDTO(0, 7, -1, true)));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(inventoryService);
    }

    @Test
    void returnsNotFoundWhenDeleteTargetDoesNotExist() {
        when(inventoryService.deleteLogically(7)).thenReturn(false);

        assertEquals(HttpStatus.NOT_FOUND, controller.delete(7).getStatusCode());
    }
}

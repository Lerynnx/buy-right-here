package com.buy_right_here.inventory_service.inventory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.buy_right_here.inventory_service.inventory.dto.InventoryDTO;
import com.buy_right_here.inventory_service.inventory.entity.Inventory;
import com.buy_right_here.inventory_service.mapper.InventoryMapper;
import com.buy_right_here.inventory_service.messaging.InventoryLabelPublisher;
import com.buy_right_here.inventory_service.repository.InventoryRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryLabelPublisher labelPublisher;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(inventoryRepository, inventoryMapper, labelPublisher);
    }

    @Test
    void getAllAppliesOffsetAndLimit() {
        Inventory first = inventory(1L, 10, true);
        Inventory second = inventory(2L, 5, true);
        when(inventoryRepository.findAllByActiveTrueOrderByProductIdAsc()).thenReturn(Flux.just(first, second));
        when(inventoryMapper.toDto(second)).thenReturn(dto(2, 2, 5, true));

        List<InventoryDTO> result = inventoryService.getAll(1, 1);

        assertEquals(List.of(dto(2, 2, 5, true)), result);
    }

    @Test
    void createResetsIdActivatesInventoryAndPublishesLabel() {
        InventoryDTO input = dto(99, 7, 3, false);
        Inventory entity = inventory(99L, 3, false);
        Inventory saved = inventory(10L, 3, true);
        InventoryDTO expected = dto(10, 7, 3, true);
        when(inventoryMapper.toEntity(input)).thenReturn(entity);
        when(inventoryRepository.save(entity)).thenReturn(Mono.just(saved));
        when(inventoryMapper.toDto(saved)).thenReturn(expected);

        InventoryDTO result = inventoryService.create(input);

        assertEquals(expected, result);
        assertNull(entity.getId());
        assertTrue(entity.isActive());
        verify(labelPublisher).publish(7L, "Last Units");
    }

    @Test
    void updateReturnsNullWhenProductDoesNotExist() {
        when(inventoryRepository.findByProductIdAndActiveTrue(7L)).thenReturn(Mono.empty());

        assertNull(inventoryService.update(7L, dto(0, 7, 10, true)));

        verifyNoInteractions(labelPublisher);
    }

    @Test
    void updatePublishesOnlyWhenStockLabelChanges() {
        Inventory current = inventory(10L, 20, true);
        InventoryDTO input = dto(0, 7, 8, true);
        InventoryDTO expected = dto(10, 7, 8, true);
        when(inventoryRepository.findByProductIdAndActiveTrue(7L)).thenReturn(Mono.just(current));
        when(inventoryRepository.save(current)).thenReturn(Mono.just(current));
        when(inventoryMapper.toDto(current)).thenReturn(expected);

        assertEquals(expected, inventoryService.update(7L, input));

        verify(labelPublisher).publish(7L, "Low Stock");
        assertEquals(8, current.getStock());
    }

    @Test
    void deleteLogicallyDeactivatesExistingInventory() {
        Inventory current = inventory(10L, 20, true);
        when(inventoryRepository.findByProductIdAndActiveTrue(7L)).thenReturn(Mono.just(current));
        when(inventoryRepository.save(current)).thenReturn(Mono.just(current));

        assertTrue(inventoryService.deleteLogically(7L));

        assertFalse(current.isActive());
        verify(inventoryRepository).save(current);
    }

    private static Inventory inventory(Long id, int stock, boolean active) {
        return new Inventory(id, 7L, stock, active);
    }

    private static InventoryDTO dto(long id, long productId, int stock, boolean active) {
        return new InventoryDTO(id, productId, stock, active);
    }
}

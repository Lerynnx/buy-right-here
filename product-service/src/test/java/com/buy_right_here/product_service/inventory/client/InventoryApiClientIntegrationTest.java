package com.buy_right_here.product_service.inventory.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import com.buy_right_here.product_service.inventory.dto.InventoryDTO;

class InventoryApiClientIntegrationTest {

    private static final InventoryDTO INVENTORY = new InventoryDTO(10L, 1L, 25, true);

    private HttpServer server;
    private InventoryApiClient client;
    private AtomicReference<String> lastRequestBody;
    private AtomicReference<String> lastRequestMethod;
    private AtomicReference<String> lastRequestUri;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        lastRequestBody = new AtomicReference<>();
        lastRequestMethod = new AtomicReference<>();
        lastRequestUri = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", this::handleRequest);
        server.start();

        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        client = new InventoryApiClient(RestClient.builder(), baseUrl);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void readsInventoryAndSupportsPagination() {
        InventoryDTO inventory = client.getInventoryByProductId(1L);
        List<InventoryDTO> page = client.getInventories(20, 10);

        assertEquals(INVENTORY, inventory);
        assertEquals(List.of(INVENTORY), page);
        assertEquals("GET", lastRequestMethod.get());
    }

    @Test
    void sendsPaginationParametersToInventoryApi() {
        client.getInventories(20, 10);

        assertEquals("/inventory?offset=20&limit=10", lastRequestUri.get());
    }

    @Test
    void propagatesNotFoundResponsesFromInventoryApi() {
        assertThrows(HttpClientErrorException.NotFound.class,
                () -> client.getInventoryByProductId(999L));
    }

    @Test
    void createsUpdatesAndDeletesInventoryUsingExpectedPayloads() throws Exception {
        InventoryDTO created = client.createInventory(INVENTORY);
        assertEquals(INVENTORY, created);
        assertEquals(objectMapper.writeValueAsString(INVENTORY), lastRequestBody.get());

        InventoryDTO updated = new InventoryDTO(10L, 1L, 30, true);
        assertEquals(updated, client.updateInventory(1L, updated));
        assertEquals(objectMapper.writeValueAsString(updated), lastRequestBody.get());

        client.deleteInventory(1L);
        assertEquals("DELETE", lastRequestMethod.get());
    }

    @Test
    void readsAllAndSendsBatchUpdates() throws Exception {
        assertEquals(List.of(INVENTORY), client.getAllInventories());

        client.createOrUpdateInventories(List.of(INVENTORY), "/inventory/batch");

        assertEquals("POST", lastRequestMethod.get());
        assertEquals(objectMapper.writeValueAsString(List.of(INVENTORY)), lastRequestBody.get());
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        lastRequestMethod.set(exchange.getRequestMethod());
        lastRequestUri.set(exchange.getRequestURI().toString());
        lastRequestBody.set(new String(exchange.getRequestBody().readAllBytes()));

        String path = exchange.getRequestURI().getPath();
        if ("GET".equals(exchange.getRequestMethod()) && path.endsWith("/inventory/1")) {
            sendJson(exchange, 200, INVENTORY);
        } else if ("GET".equals(exchange.getRequestMethod()) && path.equals("/inventory")) {
            sendJson(exchange, 200, List.of(INVENTORY));
        } else if ("POST".equals(exchange.getRequestMethod()) && path.equals("/inventory")) {
            sendJson(exchange, 201, INVENTORY);
        } else if ("PUT".equals(exchange.getRequestMethod()) && path.endsWith("/inventory/1")) {
            sendJson(exchange, 200, objectMapper.readValue(lastRequestBody.get(), InventoryDTO.class));
        } else if ("POST".equals(exchange.getRequestMethod()) && path.equals("/inventory/batch")) {
            sendJson(exchange, 204, null);
        } else if ("DELETE".equals(exchange.getRequestMethod()) && path.endsWith("/inventory/1")) {
            sendJson(exchange, 204, null);
        } else {
            sendJson(exchange, 404, Map.of("error", "not found"));
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] response = body == null ? new byte[0] : objectMapper.writeValueAsBytes(body);
        if (body != null) {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
        }
        exchange.sendResponseHeaders(status, response.length);
        try (var outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
    }
}

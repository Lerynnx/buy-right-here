package com.super_spring.product_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.super_spring.product_service.product.controller.ProductController;
import com.super_spring.product_service.product.dto.ProductDTO;
import com.super_spring.product_service.product.mapper.ProductMapper;
import com.super_spring.product_service.product.service.ImportExportProductService;
import com.super_spring.product_service.product.service.ProductService;

class ProductServiceApplicationTests {

	@Test
	void applicationClassIsConfiguredAsSpringBootApplication() {
		assertNotNull(ProductServiceApplication.class
				.getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class));
	}

	@Test
	void productControllerReturnsProductsAndTotalCount() throws Exception {
		ProductService productService = mock(ProductService.class);
		ImportExportProductService importExportService = mock(ImportExportProductService.class);
		ProductMapper productMapper = mock(ProductMapper.class);
		ProductDTO product = new ProductDTO(
				1L, 2L, "Keyboard", "Mechanical keyboard", 99.99, 0.21, 12,
				"IN_STOCK", null, List.of(), "Example", java.util.Map.of(), 4.5, 10, true);

		when(productService.getProducts(0, 10)).thenReturn(List.of(product));
		when(productService.getTotalCount()).thenReturn(1);

		MockMvc mvc = MockMvcBuilders
				.standaloneSetup(new ProductController(
						productService, importExportService, productMapper, 100))
				.build();

		var response = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
				.get("/products"))
				.andReturn()
				.getResponse();

		assertEquals(200, response.getStatus());
		assertEquals("1", response.getHeader("X-Total-Count"));
		assertEquals("[{\"id\":1,\"categoryId\":2,\"name\":\"Keyboard\",\"description\":\"Mechanical keyboard\","
				+ "\"price\":99.99,\"tax\":0.21,\"stock\":12,\"stockLabel\":\"IN_STOCK\",\"imageUrls\":[],"
				+ "\"brand\":\"Example\",\"attributes\":{},\"rating\":4.5,\"totalReviews\":10,\"active\":true}]",
				response.getContentAsString());
	}

	@Test
	void productControllerRejectsInvalidPagination() throws Exception {
		ProductService productService = mock(ProductService.class);
		MockMvc mvc = MockMvcBuilders
				.standaloneSetup(new ProductController(
						productService, mock(ImportExportProductService.class),
						mock(ProductMapper.class), 100))
				.build();

		var response = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
				.get("/products?offset=-1&limit=10"))
				.andReturn()
				.getResponse();

		assertEquals(400, response.getStatus());
	}

	@Test
	void productControllerReturnsNoContentWhenThereAreNoProducts() throws Exception {
		ProductService productService = mock(ProductService.class);
		when(productService.getProducts(0, 10)).thenReturn(List.of());
		when(productService.getTotalCount()).thenReturn(0);

		MockMvc mvc = MockMvcBuilders
				.standaloneSetup(new ProductController(
						productService, mock(ImportExportProductService.class),
						mock(ProductMapper.class), 100))
				.build();

		var response = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
				.get("/products"))
				.andReturn()
				.getResponse();

		assertEquals(204, response.getStatus());
	}

	@Test
	void productControllerCapsLimitAtConfiguredMaximum() throws Exception {
		ProductService productService = mock(ProductService.class);
		when(productService.getProducts(2, 25)).thenReturn(List.of(
				new ProductDTO(1L, 1L, "Item", null, 10.0, 0.21, null,
						null, null, List.of(), null, java.util.Map.of(), 0.0, 0, true)));
		when(productService.getTotalCount()).thenReturn(1);

		MockMvc mvc = MockMvcBuilders
				.standaloneSetup(new ProductController(
						productService, mock(ImportExportProductService.class),
						mock(ProductMapper.class), 25))
				.build();

		var response = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
				.get("/products?offset=2&limit=100"))
				.andReturn()
				.getResponse();

		assertEquals(200, response.getStatus());
		verify(productService).getProducts(2, 25);
	}

	@Test
	void productControllerHandlesInvalidAndMissingProductIds() throws Exception {
		ProductService productService = mock(ProductService.class);
		when(productService.getProductById(999L)).thenReturn(null);

		MockMvc mvc = MockMvcBuilders
				.standaloneSetup(new ProductController(
						productService, mock(ImportExportProductService.class),
						mock(ProductMapper.class), 100))
				.build();

		var invalidIdResponse = mvc.perform(
				org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/products/0"))
				.andReturn()
				.getResponse();
		var missingResponse = mvc.perform(
				org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/products/999"))
				.andReturn()
				.getResponse();

		assertEquals(400, invalidIdResponse.getStatus());
		assertEquals(404, missingResponse.getStatus());
		verify(productService, never()).getProductById(0L);
		verify(productService).getProductById(999L);
	}

}

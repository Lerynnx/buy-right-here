package com.super_spring.product_service.service;

import org.springframework.stereotype.Service;

@Service
public class ImportExportProductService {

    @Async("exportTaskExecutor")
    public CompletableFuture<Void> exportProductsToCsv() {
        // read the products from the database and export them to an csv file

        // It have to ask for the stocks to the inventory service 
        // it will be done by asked by bulk, so it will be more efficient
        return CompletableFuture.completedFuture(null);
    }


    @Async("importTaskExecutor")
    public CompletableFuture<Void> importProductsFromCsv(MultipartFile file) {
        // read the products from the csv file and import them to the database

        // It have to send the stocks to the inventory service to update the stocks of the products
        // it will be done by bulk, so it will be more efficient
        return CompletableFuture.completedFuture(null);
    }

}
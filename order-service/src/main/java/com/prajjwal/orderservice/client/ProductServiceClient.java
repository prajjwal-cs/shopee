package com.prajjwal.orderservice.client;

import com.prajjwal.orderservice.dto.ProductDto;
import com.prajjwal.orderservice.dto.StockUpdateRequest;
import com.prajjwal.orderservice.util.Packet;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "product-service",
            path = "/api/products",
            configuration = com.prajjwal.orderservice.config.FeignConfig.class)
public interface ProductServiceClient {

    @PatchMapping("/{id}/stock")
    Packet<ProductDto> updateStock(@PathVariable UUID id,
                                   @RequestBody StockUpdateRequest request);
}

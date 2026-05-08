package com.prajjwal.cartservice.client;

import com.prajjwal.cartservice.config.FeignConfig;
import com.prajjwal.cartservice.dto.ProductResponse;
import com.prajjwal.cartservice.util.Packet;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "product-service", path = "/api/v1/products", configuration = FeignConfig.class)
public interface ProductServiceClient {

    @GetMapping("/{id}")
    Packet<ProductResponse> getProductById(@PathVariable UUID id);
}
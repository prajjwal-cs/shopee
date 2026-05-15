package com.prajjwal.orderservice.client;

import com.prajjwal.orderservice.dto.CartDto;
import com.prajjwal.orderservice.util.Packet;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "cart-service",
            path = "/api/cart",
            configuration = com.prajjwal.orderservice.config.FeignConfig.class)
public interface CartServiceClient {

    @GetMapping
    Packet<CartDto> getCart();

    @DeleteMapping
    Packet<String> clearCart();
}
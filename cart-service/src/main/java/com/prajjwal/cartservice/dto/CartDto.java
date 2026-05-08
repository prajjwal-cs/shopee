package com.prajjwal.cartservice.dto;

import jdk.dynalink.linker.LinkerServices;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CartDto {
    private UUID cartId;
    private String userId;
    private List<CartItemDto> items;
    private BigDecimal total;
    private int itemCount;
}
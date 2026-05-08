package com.prajjwal.cartservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CartItemDto {
    private UUID cartItemId;
    private UUID productId;
    private String productName;
    private String productSku;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subTotal;
    private String imageUrl;

}
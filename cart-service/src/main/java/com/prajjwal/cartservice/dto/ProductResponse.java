package com.prajjwal.cartservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class ProductResponse {
    private UUID id;
    private String name;
    private String sku;
    private BigDecimal effectivePrice;
    private Integer stock;
    private List<String> imageUrls;
    private boolean active;

}
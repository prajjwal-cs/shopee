package com.prajjwal.productservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductDto {
    private UUID id;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal effectivePrice;
    private Integer stock;
    private List<String> imageUrls;
    private CategoryDto category;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
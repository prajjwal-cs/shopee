package com.prajjwal.productservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateProductRequest {
    private String name;
    private String description;

    @DecimalMin(value = "0.01")
    private BigDecimal price;

    @DecimalMin(value = "0.01")
    private BigDecimal discountPrice;

    @Min(0)
    private Integer stock;

    private List<String> imageUrls;
    private UUID categoryId;
    private Boolean active;
}
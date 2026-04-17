package com.prajjwal.productservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreateProductRequestDto {
    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotBlank(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "Discount price must be greater than 0")
    private BigDecimal discountPrice;

    @NotBlank(message = "Stock is required")
    private Integer stock;

    private List<String> imageUrls;

    @NotBlank(message = "Category ID is required")
    private UUID categoryId;
}
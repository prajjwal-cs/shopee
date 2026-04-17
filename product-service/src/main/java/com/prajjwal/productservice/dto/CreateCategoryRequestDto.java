package com.prajjwal.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequestDto {

    @NotBlank(message = "Category name is required")
    private String name;
    private String description;
}
package com.prajjwal.productservice.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link com.prajjwal.productservice.model.Category}
 */
@Data
@Builder
public class CategoryDto {
    UUID id;
    String name;
    String description;
    boolean active;
}
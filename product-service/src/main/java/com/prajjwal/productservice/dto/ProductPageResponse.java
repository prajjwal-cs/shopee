package com.prajjwal.productservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductPageResponse {
    private List<ProductDto> products;
    private int currentPage;
    private int TotalPages;
    private long totalElements;
    private boolean last;
}
package com.prajjwal.productservice.controller;

import com.prajjwal.productservice.dto.*;
import com.prajjwal.productservice.service.ProductService;
import com.prajjwal.productservice.util.Packet;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Packet<ProductPageResponse>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Packet<ProductPageResponse> packet = productService.getProducts(
                keyword, categoryId, page, size, sortBy, sortDir);

        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @GetMapping("{id}")
    public ResponseEntity<Packet<ProductDto>> getProductById(@PathVariable UUID id) {
        Packet<ProductDto> packet = productService.getProductById(id);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<Packet<ProductDto>> createProduct(@Valid @RequestBody CreateProductRequestDto request) {
        Packet<ProductDto> packet = productService.createProduct(request);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<Packet<ProductDto>> updateProduct(@PathVariable UUID id,
                                                            @Valid @RequestBody UpdateProductRequest request) {
        Packet<ProductDto> packet = productService.updateProduct(id, request);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Packet<String>> deleteProduct(@PathVariable UUID id) {
        Packet<String> packet = productService.deleteProduct(id);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<Packet<ProductDto>> updateStock(@PathVariable UUID id,
                                                          @Valid @RequestBody StockUpdateRequest request) {
        Packet<ProductDto> packet = productService.updateStock(id, request);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }
}
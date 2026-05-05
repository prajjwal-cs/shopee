package com.prajjwal.productservice.controller;

import com.prajjwal.productservice.dto.CategoryDto;
import com.prajjwal.productservice.dto.CreateCategoryRequestDto;
import com.prajjwal.productservice.service.CategoryService;
import com.prajjwal.productservice.util.Packet;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Packet<List<CategoryDto>>> getAllCategories() {
        Packet<List<CategoryDto>> packet = categoryService.getAllCategories();
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Packet<CategoryDto>> getCategoryById(@PathVariable UUID id) {
        Packet<CategoryDto> packet = categoryService.getCategoryById(id);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Packet<CategoryDto>> createCategory(@Valid @RequestBody CreateCategoryRequestDto request) {
        Packet<CategoryDto> packet = categoryService.createCategory(request);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Packet<CategoryDto>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCategoryRequestDto request) {
        Packet<CategoryDto> packet = categoryService.updateCategory(id, request);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Packet<String>> deleteCategory(@PathVariable UUID id) {
        Packet<String> packet = categoryService.deleteCategory(id);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }
}
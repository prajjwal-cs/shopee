package com.prajjwal.productservice.service;

import com.prajjwal.productservice.dto.CategoryDto;
import com.prajjwal.productservice.dto.CreateCategoryRequestDto;
import com.prajjwal.productservice.exception.CategoryAlreadyExistException;
import com.prajjwal.productservice.model.Category;
import com.prajjwal.productservice.repository.CategoryRepository;
import com.prajjwal.productservice.util.Packet;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // getAllCategories
    @Transactional(readOnly = true)
    public Packet<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categoryList =  categoryRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();

        Packet<List<CategoryDto>> packet = new Packet<>();
        return packet.ok(categoryList);
    }

    // getCategoryByID
    @Transactional(readOnly = true)
    public Packet<CategoryDto> getCategoryById(UUID id) {
        Packet<CategoryDto> packet = new Packet<>();
        return packet.ok(convertToDto(findOrThrow(id)));
    }

    // createCategory
    @Transactional
    public Packet<CategoryDto> createCategory(CreateCategoryRequestDto request) {
        Packet<CategoryDto> packet = new Packet<>();
        if (categoryRepository.existsByName(request.getName())) {
            throw new CategoryAlreadyExistException(request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();

        categoryRepository.save(category);
        return packet.ok(convertToDto(category));
    }

    // updateCategory
    @Transactional
    public Packet<CategoryDto> updateCategory(UUID id, CreateCategoryRequestDto request) {
        Packet<CategoryDto> packet = new Packet<>();
        Category category = findOrThrow(id);
        if (!category.getName().equals(request.getName())
                && categoryRepository.existsByName(request.getName())) {
            throw new CategoryAlreadyExistException(request.getName());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return packet.ok(convertToDto(categoryRepository.save(category)));
    }
    // deleteCategory
    @Transactional
    public Packet<Object> deleteCategory(UUID id) {
        Category category = findOrThrow(id);
        category.setActive(false);
        categoryRepository.save(category);
        return new Packet<>().ok("Category " + category.getName() + " changed to inactive successfully");
    }

    // findOrThrow(id)
    public Category findOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category is not available"));
    }

    // convertToDto(category c)

    private CategoryDto convertToDto(Category c) {
        return CategoryDto.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .active(c.isActive())
                .build();
    }
}
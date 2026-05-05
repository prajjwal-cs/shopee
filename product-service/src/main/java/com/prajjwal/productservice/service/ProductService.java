package com.prajjwal.productservice.service;

import com.prajjwal.productservice.dto.*;
import com.prajjwal.productservice.exception.DuplicateSkuException;
import com.prajjwal.productservice.exception.InsufficientStockException;
import com.prajjwal.productservice.exception.ResourceNotFoundException;
import com.prajjwal.productservice.model.Category;
import com.prajjwal.productservice.model.Product;
import com.prajjwal.productservice.repository.ProductRepository;
import com.prajjwal.productservice.util.Packet;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public Packet<ProductPageResponse> getProducts(String keyword, UUID categoryId,
                                                   int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.searchProducts(keyword, categoryId, pageable);

        ProductPageResponse response = ProductPageResponse.builder()
                .products(productPage.getContent().stream().map(this::convertToDto).toList())
                .currentPage(productPage.getNumber())
                .totalPages(productPage.getTotalPages())
                .totalElements(productPage.getTotalElements())
                .last(productPage.isLast())
                .build();

        return new Packet<ProductPageResponse>().ok(response);
    }

    @Transactional(readOnly = true)
    public Packet<ProductDto> getProductById(UUID id) {
        return new Packet<ProductDto>().ok(convertToDto(findActiveOrThrow(id)));
    }

    @Transactional
    public Packet<ProductDto> createProduct(CreateProductRequestDto request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException(request.getSku());
        }

        Category category = categoryService.findOrThrow(request.getCategoryId());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sku(request.getSku())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .stock(request.getStock())
                .imageUrls(request.getImageUrls() != null ? request.getImageUrls() : List.of())
                .category(category)
                .active(true)
                .build();

        ProductDto productResponse = convertToDto(productRepository.save(product));
        log.info("Product created: {}", productResponse.getSku());
        return new Packet<ProductDto>().created(productResponse);
    }

    @Transactional
    public Packet<ProductDto> updateProduct(UUID id, UpdateProductRequest request) {
        Product product = findActiveOrThrow(id);

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getDiscountPrice() != null) product.setDiscountPrice(request.getDiscountPrice());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getImageUrls() != null) product.setImageUrls(request.getImageUrls());
        if (request.getActive() != null) product.setActive(request.getActive());
        if (request.getCategoryId() != null) {
            product.setCategory(categoryService.findOrThrow(request.getCategoryId()));
        }

        log.info("Product updated: {}", product.getSku());
        return new Packet<ProductDto>().ok(convertToDto(productRepository.save(product)));
    }

    @Transactional
    public Packet<String> deleteProduct(UUID id) {
        Product product = findActiveOrThrow(id);
        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft-deleted: {}", product.getSku());
        return new Packet<String>().ok("Product deleted successfully");
    }

    @Transactional
    public Packet<ProductDto> updateStock(UUID id, StockUpdateRequest request) {
        int updatedStock;
        if (request.getOperation() == StockUpdateRequest.Operation.REDUCE) {
            updatedStock = productRepository.reduceStock(id, request.getQuantity());
            if (updatedStock == 0) {
                Product p = findActiveOrThrow(id);
                throw new InsufficientStockException(p.getName(), p.getStock(), request.getQuantity());
            }
        } else {
            findActiveOrThrow(id);
            updatedStock = productRepository.addStock(id, request.getQuantity());
        }
        log.info("Stock updatedStock for product id = {}, updatedStock stock = {}", id, updatedStock);

        return getProductById(id);
    }

    private Product findActiveOrThrow(UUID id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private ProductDto convertToDto(Product p) {
        return ProductDto.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .sku(p.getSku())
                .price(p.getPrice())
                .discountPrice(p.getDiscountPrice())
                .effectivePrice(p.getEffectivePrice())
                .stock(p.getStock())
                .imageUrls(p.getImageUrls())
                .category(CategoryDto.builder()
                        .id(p.getCategory().getId())
                        .name(p.getCategory().getName())
                        .description(p.getCategory().getDescription())
                        .active(p.getCategory().isActive())
                        .build())
                .active(p.isActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
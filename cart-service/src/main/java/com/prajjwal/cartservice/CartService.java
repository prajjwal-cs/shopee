package com.prajjwal.cartservice;

import com.prajjwal.cartservice.client.ProductServiceClient;
import com.prajjwal.cartservice.dto.*;
import com.prajjwal.cartservice.exception.CartItemNotFoundException;
import com.prajjwal.cartservice.exception.InsufficientStockException;
import com.prajjwal.cartservice.exception.ProductNotFoundException;
import com.prajjwal.cartservice.model.Cart;
import com.prajjwal.cartservice.model.CartItem;
import com.prajjwal.cartservice.model.redis.CartCache;
import com.prajjwal.cartservice.repository.CartItemRepository;
import com.prajjwal.cartservice.repository.CartRepository;
import com.prajjwal.cartservice.util.Packet;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class CartService {

    private static final String CACHE_PREFIX = "cart:";
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productClient;
    private final RedisTemplate<String, CartCache> redisTemplate;
    @Value("${app.cart.ttl-days:7}")
    private long cartTtlDays;

    @Transactional(readOnly = true)
    public Packet<CartDto> getCart(UUID userId) {
        CartCache cartCache = getFromRedis(userId);

        if (cartCache == null) {
            cartCache = loadFromDbAndCache(userId);
        }

        return new Packet<CartDto>().ok(convertToDto(cartCache));
    }

    @Transactional
    public Packet<CartDto> addItem(UUID userId, AddItemRequest request) {
        ProductResponse product = validateProduct(request.getProductId(), request.getQuantity());

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().userId(userId).items(new ArrayList<>()).build()));

        CartItem existingItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQty = existingItem.getQuantity() + request.getQuantity();
            if (newQty > product.getStock()) {
                throw new InsufficientStockException(product.getName(), product.getStock(), newQty);
            }
            existingItem.setQuantity(newQty);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .unitPrice(product.getEffectivePrice())
                    .quantity(request.getQuantity())
                    .imageUrl(product.getImageUrls() != null
                            && !product.getImageUrls().isEmpty()
                            ? product.getImageUrls().getFirst() : null)
                    .build();
            cart.getItems().add(cartItemRepository.save(newItem));
        }

        cartRepository.save(cart);

        CartCache updated = buildCartCache(cart);
        saveToRedis(userId, updated);

        log.info("Item added to cart for user: {}", userId);
        return new Packet<CartDto>().ok(convertToDto(updated));
    }

    @Transactional
    public Packet<CartDto> updateItem(UUID userId, UUID productId, UpdateItemRequest request) {
        ProductResponse product = validateProduct(productId, request.getQuantity());

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        if (request.getQuantity() > product.getStock()) {
            throw new InsufficientStockException(product.getName(), product.getStock(), request.getQuantity());
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        cartRepository.save(cart);

        CartCache updated = buildCartCache(cart);
        saveToRedis(userId, updated);

        log.info("Cart item updated for user: {}", userId);
        return new Packet<CartDto>().ok(convertToDto(updated));
    }

    @Transactional
    public Packet<CartDto> removeItem(UUID userId, UUID productId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);

        CartCache updated = buildCartCache(cart);
        saveToRedis(userId, updated);

        log.info("Item removed from cart for user: {}", userId);
        return new Packet<CartDto>().ok(convertToDto(updated));
    }

    @Transactional
    public Packet<String> clearCart(UUID userId) {
        cartRepository.findByUserIdWithItems(userId).ifPresent(
                cart -> {
                    cart.getItems().clear();
                    cartRepository.save(cart);
                }
        );

        redisTemplate.delete(CACHE_PREFIX + userId);

        log.info("Cart cleared for user: {}", userId);
        return new Packet<String>().ok("Cart cleared successfully");
    }


//    --------- HELPER METHODS ---------------

    private ProductResponse validateProduct(UUID productId, int requestedQuantity) {
        try {
            Packet<ProductResponse> response = productClient.getProductById(productId);
            if (!response.isSuccess() || response.getData() == null
                    || !response.getData().isActive()) {
                throw new ProductNotFoundException(productId);
            }
            ProductResponse product = response.getData();
            if (product.getStock() < requestedQuantity) {
                throw new InsufficientStockException(product.getName(), product.getStock(), requestedQuantity);
            }
            return product;
        } catch (ProductNotFoundException | InsufficientStockException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to reach product-service: {}", ex.getMessage());
            throw new ProductNotFoundException(productId);
        }
    }

    private CartCache loadFromDbAndCache(UUID userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElse(Cart.builder().userId(userId).items(new ArrayList<>()).build());
        CartCache cartCache = buildCartCache(cart);
        saveToRedis(userId, cartCache);
        return cartCache;
    }

    private CartCache buildCartCache(Cart cart) {
        return CartCache.builder()
                .cartId(cart.getId())
                .userId(cart.getUserId())
                .updatedAt(Instant.now())
                .items(cart.getItems().stream()
                        .map(e -> CartCache.CartItemCache.builder()
                                .cartItemId(e.getId())
                                .productId(e.getProductId())
                                .productName(e.getProductName())
                                .productSku(e.getProductSku())
                                .unitPrice(e.getUnitPrice())
                                .quantity(e.getQuantity())
                                .imageUrl(e.getImageUrl())
                                .build())
                        .toList())
                .build();
    }

    private CartCache getFromRedis(UUID userId) {
        try {
            return redisTemplate.opsForValue().get(CACHE_PREFIX + userId);
        } catch (Exception ex) {
            log.warn("Redis read failed for user {}: {}", userId, ex.getMessage());
            return null;
        }
    }

    private void saveToRedis(UUID userId, CartCache cartCache) {
        try {
            cartCache.setUpdatedAt(Instant.now());
            redisTemplate.opsForValue().set(
                    CACHE_PREFIX + userId, cartCache,
                    Duration.ofDays(cartTtlDays));
        } catch (Exception ex) {
            log.warn("Redis write failed for user {}: {}", userId, ex.getMessage());
        }
    }

    private CartDto convertToDto(CartCache cartCache) {
        var items = cartCache.getItems().stream()
                .map(e -> CartItemDto.builder()
                        .cartItemId(e.getCartItemId())
                        .productId(e.getProductId())
                        .productName(e.getProductName())
                        .productSku(e.getProductSku())
                        .unitPrice(e.getUnitPrice())
                        .quantity(e.getQuantity())
                        .subTotal(e.getUnitPrice().multiply(BigDecimal.valueOf(e.getQuantity())))
                        .imageUrl(e.getImageUrl())
                        .build())
                .toList();

        return CartDto.builder()
                .cartId(cartCache.getCartId())
                .userId(cartCache.getUserId())
                .items(items)
                .total(cartCache.getTotal())
                .itemCount(items.size())
                .build();
    }
}

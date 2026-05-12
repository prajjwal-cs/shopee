package com.prajjwal.cartservice.exception;

import java.util.UUID;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(UUID productId) {
        super("Item not found in cart for product: " + productId);
    }
}
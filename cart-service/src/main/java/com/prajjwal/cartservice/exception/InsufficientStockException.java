package com.prajjwal.cartservice.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName, int available, int requested) {
        super("Insufficient stock for '" + productName + "'. Available: " + available
                + ", Requested: " + requested);
    }
}
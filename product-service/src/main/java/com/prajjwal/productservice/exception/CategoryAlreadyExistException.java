package com.prajjwal.productservice.exception;

public class CategoryAlreadyExistException extends RuntimeException {
    public CategoryAlreadyExistException(String name) {
        super("Category already exists: " + name);
    }
}

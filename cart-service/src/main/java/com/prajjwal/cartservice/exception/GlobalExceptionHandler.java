package com.prajjwal.cartservice.exception;

import com.prajjwal.cartservice.util.Packet;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Packet<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e ->
                errors.put(((FieldError) e).getField(), e.getDefaultMessage()));
        Packet<Void> packet = new Packet<Void>().badRequest(errors);
        return new ResponseEntity<>(packet, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Packet<Void>> handleProductNowFound(ProductNotFoundException ex) {
        Packet<Void> packet = new Packet<Void>().notFound(ex.getMessage());
        return new ResponseEntity<>(packet, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<Packet<Void>> handleCartItemNotFound(CartItemNotFoundException ex) {
        Packet<Void> packet = new Packet<Void>().notFound(ex.getMessage());
        return new ResponseEntity<>(packet, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Packet<Void>> handleInsufficientStock(
            InsufficientStockException ex) {
        Packet<Void> packet = new Packet<Void>().badRequest(ex.getMessage());
        return new ResponseEntity<>(packet, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Packet<Void>> handleAccessDenied(AccessDeniedException ex) {
        Packet<Void> packet = new Packet<Void>().forbidden("Access denied");
        return new ResponseEntity<>(packet, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Packet<Void>> handleAll(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        Packet<Void> packet = new Packet<Void>().internalError("An unexpected error occurred");
        return new ResponseEntity<>(packet, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
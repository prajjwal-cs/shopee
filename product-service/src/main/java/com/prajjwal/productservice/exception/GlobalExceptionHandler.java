package com.prajjwal.productservice.exception;

import com.prajjwal.productservice.util.Packet;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.util.Pack;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
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

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Packet<Void>> handleNotFound(ResourceNotFoundException ex) {
        Packet<Void> packet = new Packet<Void>().notFound(ex.getMessage());
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<Packet<Void>> handleDuplicateSku(DuplicateSkuException ex) {
        Packet<Void> packet = new Packet<Void>().badRequest(ex.getMessage());
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @ExceptionHandler(CategoryAlreadyExistException.class)
    public ResponseEntity<Packet<Void>> handleCategoryExists(CategoryAlreadyExistException ex) {
        Packet<Void> packet = new Packet<Void>().badRequest(ex.getMessage());
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Packet<Void>> handleInsufficientStock(InsufficientStockException ex) {
        Packet<Void> packet = new Packet<Void>().badRequest(ex.getMessage());
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Packet<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e ->
                fieldErrors.put(((FieldError) e).getField(), e.getDefaultMessage()));
        Packet<Void> packet = new Packet<Void>().badRequest(fieldErrors);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Packet<Void>> handleAccessDenied(AccessDeniedException ex) {
        Packet<Void> packet = new Packet<Void>().forbidden("Access denied");
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Packet<Void>> handleAll(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        Packet<Void> packet = new Packet<Void>().internalError("An unexpected error occurred");
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }
}
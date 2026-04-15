package com.prajjwal.userservice.exception;

import com.prajjwal.userservice.dto.Packet;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Packet<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });

        Packet<Void> packet = new Packet<>();
        packet.badRequest(fieldErrors);
        return new ResponseEntity<>(packet, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Packet<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            String field = cv.getPropertyPath().toString();
            errors.put(field, cv.getMessage());
        });
        Packet<Void> packet = new Packet<>();
        packet.badRequest(errors);
        return new ResponseEntity<>(packet, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Packet<Void>> handleBadCredentials(BadCredentialsException ex) {
        Packet<Void> packet = new Packet<>();
        packet.unauthorized("Invalid email or password");
        return new ResponseEntity<>(packet, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Packet<Void>> handlerDisabled(DisabledException ex) {
        Packet<Void> packet = new Packet<>();
        packet.forbidden("Account is disabled");
        return new ResponseEntity<>(packet, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Packet<Void>> handleLocked(LockedException ex) {
        Packet<Void> packet = new Packet<>();
        packet.forbidden("Account is locked");
        return new ResponseEntity<>(packet, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Packet<Void>> handleUserNotFound(UsernameNotFoundException ex) {
        Packet<Void> packet = new Packet<>();
        packet.notFound("User");
        return new ResponseEntity<>(packet, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Packet<Void>> handleExpiredJwt(ExpiredJwtException ex) {
        Packet<Void> packet = new Packet<>();
        packet.unauthorized("Token has expired");
        return new ResponseEntity<>(packet, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Packet<Void>> handleInvalidJwt(JwtException ex) {
        Packet<Void> packet = new Packet<>();
        packet.unauthorized("Invalid token");
        return new ResponseEntity<>(packet, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Packet<Void>> handleEmailExist(EmailAlreadyExistsException ex) {
        Packet<Void> packet = new Packet<>();
        packet.badRequest(ex.getMessage());
        return new ResponseEntity<>(packet, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Packet<Void>> handleInvalidToken(InvalidTokenException ex) {
        Packet<Void> packet = new Packet<>();
        packet.badRequest(ex.getMessage());
        return new ResponseEntity<>(packet, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Packet<Void>> handleTokenExpired(TokenExpiredException ex) {
        Packet<Void> packet = new Packet<>();
        packet.badRequest(ex.getMessage());
        return new ResponseEntity<>(packet, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Packet<Void>> handleInvalidPassword(InvalidPasswordException ex) {
        Packet<Void> packet = new Packet<>();
        packet.badRequest(ex.getMessage());
        return new ResponseEntity<>(packet, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Packet<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        Packet<Void> packet = new Packet<>();
        packet.badRequest(ex.getMessage());
        return new ResponseEntity<>(packet, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Packet<Void>> handleAll(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        Packet<Void> packet = new Packet<>();
        packet.internalError("An unexpected error occurred");
        return new ResponseEntity<>(packet, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
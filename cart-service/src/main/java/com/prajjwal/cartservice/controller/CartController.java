package com.prajjwal.cartservice.controller;

import com.prajjwal.cartservice.dto.AddItemRequest;
import com.prajjwal.cartservice.dto.CartDto;
import com.prajjwal.cartservice.dto.UpdateItemRequest;
import com.prajjwal.cartservice.service.CartService;
import com.prajjwal.cartservice.util.Packet;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<Packet<CartDto>> getCart(Authentication auth) {
        Packet<CartDto> packet = cartService.getCart(UUID.fromString(auth.getName()));
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @PostMapping("/items")
    public ResponseEntity<Packet<CartDto>> addItem(Authentication auth, @Valid @RequestBody AddItemRequest request) {
        Packet<CartDto> packet = cartService.addItem(
                UUID.fromString(auth.getName()), request);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<Packet<CartDto>> updateItem(Authentication auth,
                                                      @PathVariable UUID productId,
                                                      @Valid @RequestBody UpdateItemRequest request) {
        Packet<CartDto> packet = cartService.updateItem(
                UUID.fromString(auth.getName()), productId, request);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Packet<CartDto>> removeItem(Authentication auth,
                                                      @PathVariable UUID productId) {
        Packet<CartDto> packet = cartService.removeItem(UUID.fromString(auth.getName()), productId);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @DeleteMapping
    public ResponseEntity<Packet<String>> clearCart(Authentication auth) {
        Packet<String> packet = cartService.clearCart(UUID.fromString(auth.getName()));
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }
}
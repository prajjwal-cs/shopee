package com.prajjwal.userservice.controller;

import com.prajjwal.userservice.dto.*;
import com.prajjwal.userservice.model.Role;
import com.prajjwal.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.Pack;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Packet<AuthResponse>> login(@Valid @RequestBody LoginRequestDto request) {
        Packet<AuthResponse> packet = authService.login(request);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @PostMapping("/customer/register")
    public ResponseEntity<Packet<UserDto>> customerRegister(
            @Valid @RequestBody RegistrationRequestDto registrationRequest) {

        Packet<UserDto> packet = authService.register(registrationRequest, Role.CUSTOMER);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @PostMapping("/refresh")
    public ResponseEntity<Packet<AuthResponse>> refresh(@Valid @RequestBody TokenRefreshRequestDto request) {
        Packet<AuthResponse> packet = authService.refreshToken(request.getRefreshToken());
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully!"));
    }

}
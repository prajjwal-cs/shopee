package com.prajjwal.userservice.controller;

import com.prajjwal.userservice.dto.ForgetPasswordRequestDto;
import com.prajjwal.userservice.dto.Packet;
import com.prajjwal.userservice.dto.ResetPasswordRequestDto;
import com.prajjwal.userservice.dto.UpdatePasswordRequestDto;
import com.prajjwal.userservice.service.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/forgot-password")
    public ResponseEntity<Packet<String>> forgotPassword(@Valid @RequestBody ForgetPasswordRequestDto request) {
        Packet<String> packet = passwordService.forgotPassword(request);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Packet<String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        Packet<String> packet = passwordService.resetPassword(request);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }

    @PostMapping("/update-password")
    public ResponseEntity<Packet<String>> updatePassword(
            @AuthenticationPrincipal Authentication authentication,
            @Valid @RequestBody UpdatePasswordRequestDto request) {

        String email = authentication.getName();
        Packet<String> packet = passwordService.updatePassword(email, request);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }


}
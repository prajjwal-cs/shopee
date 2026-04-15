package com.prajjwal.userservice.controller;

import com.prajjwal.userservice.dto.Packet;
import com.prajjwal.userservice.dto.UserDto;
import com.prajjwal.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<Packet<UserDto>> getProfile(Authentication authentication) {
        String email = authentication.getName();
        Packet<UserDto> packet = userService.getProfile(email);
        return new ResponseEntity<>(packet, packet.getHttpStatus());
    }
}
package com.prajjwal.userservice.service;

import com.prajjwal.userservice.dto.Packet;
import com.prajjwal.userservice.dto.UserDto;
import com.prajjwal.userservice.model.User;
import com.prajjwal.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Packet<UserDto> getProfile(String email) {
        User user = findByEmailOrThrow(email);

        Packet<UserDto> packet = new Packet<>();
        return packet.ok(buildUserResponse(user));
    }


    private UserDto buildUserResponse(User user) {
        return UserDto.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .build();
    }

    private User findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
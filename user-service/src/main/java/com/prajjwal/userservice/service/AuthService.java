package com.prajjwal.userservice.service;

import com.prajjwal.userservice.dto.AuthResponse;
import com.prajjwal.userservice.dto.LoginRequestDto;
import com.prajjwal.userservice.dto.Packet;
import com.prajjwal.userservice.dto.UserDto;
import com.prajjwal.userservice.model.RefreshToken;
import com.prajjwal.userservice.model.User;
import com.prajjwal.userservice.repository.RefreshTokenRepository;
import com.prajjwal.userservice.repository.UserRepository;
import com.prajjwal.userservice.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    public Packet<AuthResponse> login(LoginRequestDto loginRequest) {
        Packet<AuthResponse> packet = new Packet<>();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            return packet.badRequest("Invalid email or Password");
        } catch (DisabledException ex) {
            return packet.forbidden("Email not verified");
        } catch (LockedException ex) {
            return packet.forbidden("Account is locked");
        }

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtTokenUtil.generateToken(user);
        String refreshToken = createRefreshToken(user);

        AuthResponse response = new AuthResponse(
                accessToken,
                refreshToken,
                buildUserResponse(user),
                user.getEmail()
        );

        return packet.ok(response);
    }

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryTime(Instant.now().plusMillis(refreshTokenExpiration))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken).getToken();
    }

    private UserDto buildUserResponse(User user) {
        return UserDto.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .build();
    }
}
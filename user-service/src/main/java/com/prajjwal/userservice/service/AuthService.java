package com.prajjwal.userservice.service;

import com.prajjwal.userservice.dto.*;
import com.prajjwal.userservice.exception.EmailAlreadyExistsException;
import com.prajjwal.userservice.exception.InvalidTokenException;
import com.prajjwal.userservice.exception.TokenExpiredException;
import com.prajjwal.userservice.model.RefreshToken;
import com.prajjwal.userservice.model.Role;
import com.prajjwal.userservice.model.User;
import com.prajjwal.userservice.repository.RefreshTokenRepository;
import com.prajjwal.userservice.repository.UserRepository;
import com.prajjwal.userservice.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    @Transactional
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
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        refreshTokenRepository.revokeAllByUser(user);

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

    @Transactional
    public Packet<UserDto> register(RegistrationRequestDto registrationRequest, Role role) {
        // check for user
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new EmailAlreadyExistsException(registrationRequest.getEmail());
        }
        Packet<UserDto> packet = new Packet<>();

        User user = new User();
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setFirstName(registrationRequest.getFirstName());
        user.setLastName(registrationRequest.getLastName());
        user.setRole(role);

        userRepository.save(user);

        return packet.ok(buildUserResponse(user));
    }

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        refreshTokenRepository.revokeAllByUser(user);
        log.info("User logged out email: {}", email);
    }

    public void forgetPassword() {

    }

    @Transactional
    public Packet<AuthResponse> refreshToken(String token) {
        RefreshToken refreshToken = validateRefreshToken(token);
        Packet<AuthResponse> packet = new Packet<>();
        User user = refreshToken.getUser();

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = jwtTokenUtil.generateToken(user);
        String newRefreshToken = createRefreshToken(user);

        AuthResponse authResponse = new AuthResponse(newAccessToken,
                newRefreshToken,
                buildUserResponse(user),
                user.getEmail()
        );

        return packet.ok(authResponse);
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

    private RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            refreshTokenRepository.revokeAllByUser(refreshToken.getUser());
            throw new InvalidTokenException("Security violation detected.");
        }
        if (refreshToken.getExpiryTime().isBefore(Instant.now())) {
            throw new TokenExpiredException("Session expired, Please login again.");
        }

        return refreshToken;
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
package com.prajjwal.userservice.service;

import com.prajjwal.userservice.dto.ForgetPasswordRequestDto;
import com.prajjwal.userservice.dto.Packet;
import com.prajjwal.userservice.dto.ResetPasswordRequestDto;
import com.prajjwal.userservice.dto.UpdatePasswordRequestDto;
import com.prajjwal.userservice.exception.InvalidPasswordException;
import com.prajjwal.userservice.exception.InvalidTokenException;
import com.prajjwal.userservice.exception.TokenExpiredException;
import com.prajjwal.userservice.model.PasswordResetToken;
import com.prajjwal.userservice.model.User;
import com.prajjwal.userservice.repository.PasswordResetTokenRepository;
import com.prajjwal.userservice.repository.RefreshTokenRepository;
import com.prajjwal.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.password-reset.expiration}")
    private Long resetTokenExpiration;

    @Transactional
    public Packet<String> forgotPassword(ForgetPasswordRequestDto request) {
        Packet<String> packet = new Packet<>();

        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            resetTokenRepository.deleteAllByUserId(user.getId());

            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiryTime(Instant.now().plusMillis(resetTokenExpiration))
                    .used(false)
                    .build();

            resetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            log.info("Password reset requested for user: {}", user.getEmail());
        });

        return packet.ok("If this email is registered, a reset link has been sent.");
    }

    @Transactional
    public Packet<String> resetPassword(ResetPasswordRequestDto request) {
        Packet<String> packet = new Packet<>();

        PasswordResetToken resetToken = resetTokenRepository
                .findByToken(request.getToken())
                .orElse(null);

        if (resetToken == null) {
            throw new InvalidTokenException("Invalid or expired reset link");
        }
        if (resetToken.isUsed()) {
            throw new InvalidTokenException("This reset link has already been used.");
        }

        if (resetToken.getExpiryTime().isBefore(Instant.now())) {
            throw new TokenExpiredException("Reset link has expired. Please request a new one.");
        }

        User user = resetToken.getUser();

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            return packet.badRequest("New password must be different from your current password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);

        refreshTokenRepository.revokeAllByUser(user);

        log.info("Password reset completed for user: {}", user.getEmail());
        return packet.ok("Password has been reset successfully. Please log in.");
    }

    @Transactional
    public Packet<String> updatePassword(String email, UpdatePasswordRequestDto request) {
        Packet<String> packet = new Packet<>();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            return packet.badRequest("New Password must be different from your current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUser(user);

        log.info("Password updated for user: {}", user.getEmail());

        return packet.ok("Password updated successfully. Please login again.");
    }
}
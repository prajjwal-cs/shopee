package com.prajjwal.userservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token", indexes = {
        @Index(name = "idx_refresh_token", columnList = "token", unique = true),
        @Index(name = "idx_refresh_token_user", columnList = "user_id")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Instant expiryTime;

    private boolean revoked;

    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiryTime);
    }
}
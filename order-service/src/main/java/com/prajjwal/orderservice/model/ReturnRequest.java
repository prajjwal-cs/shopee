package com.prajjwal.orderservice.model;

import com.prajjwal.orderservice.model.status.ReturnStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "return_requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private ReturnType returnType;

    @Enumerated(EnumType.STRING)
    private ReturnStatus status = ReturnStatus.REQUESTED;

    @Column(nullable = false)
    private String reason;

    private String adminNote;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
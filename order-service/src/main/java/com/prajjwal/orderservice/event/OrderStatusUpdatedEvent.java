package com.prajjwal.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdatedEvent {
    private UUID orderId;
    private UUID userId;
    private String orderNumber;
    private String oldStatus;
    private String newStatus;
    private String userEmail;
}
package com.prajjwal.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPlacedEvent {
    private UUID orderId;
    private UUID userId;
    private String orderNumber;
    private BigDecimal totalAmount;
    private String userEmail;
}
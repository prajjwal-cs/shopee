package com.prajjwal.cartservice.model.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartCache implements Serializable {

    private UUID cartId;
    private String userId;

    @Builder.Default
    private List<CartItemCache> items = new ArrayList<>();

    private Instant updatedAt;

    public BigDecimal getTotal() {
        return items.stream().map(e -> e.getUnitPrice()
                .multiply(BigDecimal.valueOf(e.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartItemCache implements Serializable{
        private UUID cartItemId;
        private UUID productId;
        private String productName;
        private String productSku;
        private BigDecimal unitPrice;
        private Integer quantity;
        private String imageUrl;
    }
}
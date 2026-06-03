package com.ecommerce.product.messaging;

import java.util.List;

public record OrderPlacedMessage(Long orderId, String customerEmail, List<OrderItem> items) {
    public record OrderItem(Long productId, int quantity) {}
}

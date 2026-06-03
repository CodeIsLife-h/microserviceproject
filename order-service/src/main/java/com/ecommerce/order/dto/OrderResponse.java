package com.ecommerce.order.dto;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        String customerEmail,
        String status,
        BigDecimal total,
        LocalDateTime createdAt,
        List<ItemDto> items
) {
    public record ItemDto(Long productId, String productName, Integer quantity, BigDecimal unitPrice) {
        static ItemDto from(OrderItem i) {
            return new ItemDto(i.getProductId(), i.getProductName(), i.getQuantity(), i.getUnitPrice());
        }
    }

    public static OrderResponse from(Order o) {
        return new OrderResponse(
                o.getId(), o.getCustomerId(), o.getCustomerEmail(), o.getStatus(), o.getTotal(), o.getCreatedAt(),
                o.getItems() == null ? List.of() : o.getItems().stream().map(ItemDto::from).toList()
        );
    }
}

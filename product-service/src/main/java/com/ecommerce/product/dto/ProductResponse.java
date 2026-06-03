package com.ecommerce.product.dto;

import com.ecommerce.product.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String descriptionHtml,
        BigDecimal price,
        String imageUrl,
        Integer stockCount,
        LocalDateTime createdAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(), p.getName(), p.getDescriptionHtml(),
                p.getPrice(), p.getImageUrl(), p.getStockCount(), p.getCreatedAt()
        );
    }
}

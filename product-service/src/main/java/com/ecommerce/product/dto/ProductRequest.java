package com.ecommerce.product.dto;

import java.math.BigDecimal;

public record ProductRequest(String name, String descriptionHtml, BigDecimal price, String imageUrl, Integer stockCount) {}

package com.ecommerce.product.dto;

import java.math.BigDecimal;

public record ProductRequest(String name, String description, BigDecimal price, String imageUrl, Integer stockCount) {}

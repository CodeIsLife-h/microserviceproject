package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class ProductService {

    private static final String CACHE_PREFIX = "product:";

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${product.cache.ttl-seconds:300}")
    private long cacheTtlSeconds;

    public ProductService(ProductRepository productRepository, RedisTemplate<String, Object> redisTemplate) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setDescriptionHtml(request.descriptionHtml());
        product.setPrice(request.price());
        product.setImageUrl(request.imageUrl());
        product.setStockCount(request.stockCount());
        return ProductResponse.from(productRepository.save(product));
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse getProductById(Long id) {
        String cacheKey = CACHE_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Map<?,?> map) {
            // Return cached metadata but fetch live stock from DB
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
            return ProductResponse.from(product);
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));

        // Cache metadata (full product — stock is always re-read from DB on next request)
        redisTemplate.opsForValue().set(cacheKey, ProductResponse.from(product), Duration.ofSeconds(cacheTtlSeconds));
        return ProductResponse.from(product);
    }

    @Transactional
    public void updateStock(Long id, int newCount) {
        int updated = productRepository.updateStock(id, newCount);
        if (updated == 0) {
            throw new NoSuchElementException("Product not found: " + id);
        }
        // Invalidate cache so next read fetches fresh data including updated stock
        redisTemplate.delete(CACHE_PREFIX + id);
    }

    @Transactional
    public boolean decrementStock(Long productId, int qty) {
        int updated = productRepository.decrementStock(productId, qty);
        if (updated > 0) {
            redisTemplate.delete(CACHE_PREFIX + productId);
        }
        return updated > 0;
    }
}

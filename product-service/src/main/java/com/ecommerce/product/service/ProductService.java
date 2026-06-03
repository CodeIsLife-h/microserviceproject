package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.*;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final String CACHE_PREFIX = "product:";

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${product.cache.ttl-seconds:300}")
    private long cacheTtlSeconds;

    public ProductService(ProductRepository productRepository,
                          RedisTemplate<String, Object> redisTemplate,
                          ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
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
        try {
            List<ProductResponse> products = productRepository.findAll().stream()
                    .map(ProductResponse::from)
                    .toList();

            // Cache each product individually for fallback recovery
            for (ProductResponse p : products) {
                redisTemplate.opsForValue().set(
                        CACHE_PREFIX + p.id(), p, Duration.ofSeconds(cacheTtlSeconds));
            }
            return products;

        } catch (Exception e) {
            log.warn("Database unavailable for product listing, attempting cache fallback", e);
            return recoverAllFromCache();
        }
    }

    public ProductResponse getProductById(Long id) {
        String cacheKey = CACHE_PREFIX + id;

        try {
            // Always try DB for live stock data
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));

            ProductResponse response = ProductResponse.from(product);
            // Cache for future fallback recovery
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofSeconds(cacheTtlSeconds));
            return response;

        } catch (NoSuchElementException e) {
            throw e; // Product genuinely doesn't exist — not a DB failure
        } catch (Exception e) {
            // DB connection failure — attempt cache fallback
            log.warn("Database unavailable for product {}, attempting cache fallback", id, e);
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("Serving product {} from cache (stock may be stale)", id);
                return convertCached(cached);
            }
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Product service temporarily unavailable");
        }
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

    // ── Cache fallback helpers ──────────────────────────────────────

    /**
     * Recovers product listing from individually cached Redis entries.
     * Used when PostgreSQL is unavailable — returns stale-but-usable data.
     */
    private List<ProductResponse> recoverAllFromCache() {
        Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Product catalog temporarily unavailable");
        }

        List<ProductResponse> cached = new ArrayList<>();
        for (String key : keys) {
            Object val = redisTemplate.opsForValue().get(key);
            if (val != null) {
                cached.add(convertCached(val));
            }
        }

        log.info("Recovered {} products from cache (stock data may be stale)", cached.size());
        return cached;
    }

    /**
     * Converts a Redis-deserialized object (Map or ProductResponse) back to ProductResponse.
     * GenericJackson2JsonRedisSerializer may return a LinkedHashMap depending on type metadata.
     */
    private ProductResponse convertCached(Object cached) {
        if (cached instanceof ProductResponse pr) {
            return pr;
        }
        return objectMapper.convertValue(cached, ProductResponse.class);
    }
}

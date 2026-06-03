package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock RedisTemplate<String, Object> redisTemplate;
    @Mock ValueOperations<String, Object> valueOperations;
    @InjectMocks ProductService productService;

    private Product sampleProduct() {
        Product p = new Product();
        p.setName("Test Product");
        p.setDescriptionHtml("<p>Description</p>");
        p.setPrice(new BigDecimal("29.99"));
        p.setImageUrl("http://example.com/img.jpg");
        p.setStockCount(10);
        return p;
    }

    @Test
    void createProduct_savesAndReturnsResponse() {
        Product saved = sampleProduct();
        when(productRepository.save(any())).thenReturn(saved);

        ProductResponse result = productService.createProduct(
                new ProductRequest("Test Product", "<p>Description</p>", new BigDecimal("29.99"), "http://example.com/img.jpg", 10)
        );

        assertThat(result.name()).isEqualTo("Test Product");
        assertThat(result.stockCount()).isEqualTo(10);
        verify(productRepository).save(any());
    }

    @Test
    void getAllProducts_returnsAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct(), sampleProduct()));

        List<ProductResponse> results = productService.getAllProducts();

        assertThat(results).hasSize(2);
    }

    @Test
    void getProductById_cacheMiss_fetchesFromDbAndCaches() {
        Product product = sampleProduct();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("product:1")).thenReturn(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse result = productService.getProductById(1L);

        assertThat(result.name()).isEqualTo("Test Product");
        verify(valueOperations).set(eq("product:1"), any(), any());
    }

    @Test
    void getProductById_notFound_throwsException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn(null);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void decrementStock_sufficientStock_returnsTrue() {
        when(productRepository.decrementStock(1L, 2)).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        boolean result = productService.decrementStock(1L, 2);

        assertThat(result).isTrue();
        verify(redisTemplate).delete("product:1");
    }

    @Test
    void decrementStock_insufficientStock_returnsFalse() {
        when(productRepository.decrementStock(1L, 100)).thenReturn(0);

        boolean result = productService.decrementStock(1L, 100);

        assertThat(result).isFalse();
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void updateStock_invalidatesCache() {
        when(productRepository.updateStock(1L, 50)).thenReturn(1);

        productService.updateStock(1L, 50);

        verify(redisTemplate).delete("product:1");
    }
}

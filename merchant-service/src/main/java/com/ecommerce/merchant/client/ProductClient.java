package com.ecommerce.merchant.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Value("${services.product-url}") String productUrl) {
        this.restClient = RestClient.builder().baseUrl(productUrl).build();
    }

    public List<Map<String, Object>> getAllProducts() {
        return restClient.get()
                .uri("/api/products")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public void updateStock(Long productId, int newStock) {
        restClient.put()
                .uri("/api/products/{id}/stock", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("stockCount", newStock))
                .retrieve()
                .toBodilessEntity();
    }
}

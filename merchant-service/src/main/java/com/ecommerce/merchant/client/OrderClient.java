package com.ecommerce.merchant.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(@Value("${services.order-url}") String orderUrl) {
        this.restClient = RestClient.builder().baseUrl(orderUrl).build();
    }

    public List<Map<String, Object>> getAllOrders() {
        return restClient.get()
                .uri("/api/orders/all")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}

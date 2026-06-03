package com.ecommerce.merchant.service;

import com.ecommerce.merchant.client.OrderClient;
import com.ecommerce.merchant.client.ProductClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MerchantService {

    private final ProductClient productClient;
    private final OrderClient orderClient;

    public MerchantService(ProductClient productClient, OrderClient orderClient) {
        this.productClient = productClient;
        this.orderClient = orderClient;
    }

    public List<Map<String, Object>> getSales() {
        return orderClient.getAllOrders();
    }

    public List<Map<String, Object>> getProducts() {
        return productClient.getAllProducts();
    }

    public void replenishStock(Long productId, int newStock) {
        productClient.updateStock(productId, newStock);
    }
}

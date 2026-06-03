package com.ecommerce.merchant.controller;

import com.ecommerce.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
@Tag(name = "Merchant", description = "Sales dashboard and stock management")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping("/sales")
    @Operation(summary = "View all orders (sales dashboard)")
    public List<Map<String, Object>> getSales() {
        return merchantService.getSales();
    }

    @GetMapping("/products")
    @Operation(summary = "View all products with live stock levels")
    public List<Map<String, Object>> getProducts() {
        return merchantService.getProducts();
    }

    @PutMapping("/products/{id}/stock")
    @Operation(summary = "Update stock for a product — triggers DB update and cache invalidation in Product Service")
    public ResponseEntity<?> updateStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Integer newStock = body.get("stockCount");
        if (newStock == null || newStock < 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "stockCount must be a non-negative integer"));
        }
        merchantService.replenishStock(id, newStock);
        return ResponseEntity.ok(Map.of("message", "Stock updated successfully"));
    }
}

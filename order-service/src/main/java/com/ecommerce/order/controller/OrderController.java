package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Place and track orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Place an order — returns 202 PENDING immediately, status updates asynchronously")
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestBody OrderRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail) {

        OrderResponse response = orderService.placeOrder(request, Long.parseLong(userId), userEmail);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/my")
    @Operation(summary = "Get all orders for the authenticated customer")
    public List<OrderResponse> getMyOrders(@RequestHeader("X-User-Id") String userId) {
        return orderService.getOrdersByCustomer(Long.parseLong(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID — poll this to check PENDING → CONFIRMED/FAILED")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.getOrderById(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

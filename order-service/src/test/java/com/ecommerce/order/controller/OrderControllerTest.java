package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean OrderService orderService;

    private OrderResponse pendingResponse() {
        return new OrderResponse(1L, 1L, "test@example.com", "PENDING",
                new BigDecimal("59.98"), LocalDateTime.now(), List.of());
    }

    @Test
    void placeOrder_returns202WithPendingStatus() throws Exception {
        when(orderService.placeOrder(any(), eq(1L), eq("test@example.com")))
                .thenReturn(pendingResponse());

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", "1")
                .header("X-User-Email", "test@example.com")
                .content(objectMapper.writeValueAsString(new OrderRequest(List.of(
                        new OrderItemRequest(10L, "Widget", 2, new BigDecimal("29.99")))))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getOrder_existingId_returns200() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(pendingResponse());

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerEmail").value("test@example.com"));
    }

    @Test
    void getMyOrders_returnsCustomerOrders() throws Exception {
        when(orderService.getOrdersByCustomer(1L)).thenReturn(List.of(pendingResponse()));

        mockMvc.perform(get("/api/orders/my").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}

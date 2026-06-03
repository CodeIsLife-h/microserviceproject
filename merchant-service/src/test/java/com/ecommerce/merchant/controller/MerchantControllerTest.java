package com.ecommerce.merchant.controller;

import com.ecommerce.merchant.service.MerchantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MerchantController.class)
class MerchantControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean MerchantService merchantService;

    @Test
    void getSales_returns200WithOrders() throws Exception {
        when(merchantService.getSales()).thenReturn(List.of(
                Map.of("id", 1, "status", "CONFIRMED", "total", "59.98")
        ));

        mockMvc.perform(get("/api/merchant/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    void getProducts_returns200WithProducts() throws Exception {
        when(merchantService.getProducts()).thenReturn(List.of(
                Map.of("id", 1, "name", "Widget", "stockCount", 10)
        ));

        mockMvc.perform(get("/api/merchant/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Widget"));
    }

    @Test
    void updateStock_validRequest_returns200() throws Exception {
        doNothing().when(merchantService).replenishStock(1L, 50);

        mockMvc.perform(put("/api/merchant/products/1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("stockCount", 50))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock updated successfully"));
    }

    @Test
    void updateStock_negativeStock_returns400() throws Exception {
        mockMvc.perform(put("/api/merchant/products/1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("stockCount", -1))))
                .andExpect(status().isBadRequest());
    }
}

package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock RabbitTemplate rabbitTemplate;
    @InjectMocks OrderService orderService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "exchange", "ecommerce.events");
        ReflectionTestUtils.setField(orderService, "orderPlacedKey", "order.placed");
    }

    private Order savedOrder(Long id, String status) {
        Order o = new Order();
        o.setCustomerId(1L);
        o.setCustomerEmail("test@example.com");
        o.setStatus(status);
        o.setTotal(new BigDecimal("59.98"));
        o.setItems(List.of());
        // Simulate JPA-assigned ID via reflection
        try {
            var field = Order.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(o, id);
        } catch (Exception ignored) {}
        return o;
    }

    @Test
    void placeOrder_savesAsPendingAndPublishesEvent() {
        Order saved = savedOrder(1L, "PENDING");
        when(orderRepository.save(any())).thenReturn(saved);

        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(10L, "Widget", 2, new BigDecimal("29.99"))
        ));

        OrderResponse response = orderService.placeOrder(request, 1L, "test@example.com");

        assertThat(response.status()).isEqualTo("PENDING");
        verify(rabbitTemplate).convertAndSend(anyString(), eq("order.placed"), any(Object.class));
    }

    @Test
    void confirmOrder_updatesStatusToConfirmed() {
        Order order = savedOrder(1L, "PENDING");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        orderService.confirmOrder(1L);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void failOrder_updatesStatusToFailed() {
        Order order = savedOrder(1L, "PENDING");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        orderService.failOrder(1L);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("FAILED");
    }

    @Test
    void getOrderById_notFound_throwsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getOrdersByCustomer_returnsCustomerOrders() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(savedOrder(1L, "CONFIRMED")));

        List<OrderResponse> orders = orderService.getOrdersByCustomer(1L);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).status()).isEqualTo("CONFIRMED");
    }
}

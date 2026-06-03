package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-keys.orderPlaced}")
    private String orderPlacedKey;

    public OrderService(OrderRepository orderRepository, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public OrderResponse placeOrder(OrderRequest request, Long customerId, String customerEmail) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setCustomerEmail(customerEmail);
        order.setStatus("PENDING");

        List<OrderItem> items = request.items().stream().map(req -> {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(req.productId());
            item.setProductName(req.productName());
            item.setQuantity(req.quantity());
            item.setUnitPrice(req.unitPrice());
            return item;
        }).toList();

        order.setItems(items);
        order.setTotal(items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        Order saved = orderRepository.save(order);

        // Publish order.placed event — Product Service will check stock and respond
        Map<String, Object> event = Map.of(
                "orderId", saved.getId(),
                "customerEmail", customerEmail,
                "items", request.items().stream()
                        .map(i -> Map.of("productId", i.productId(), "quantity", i.quantity()))
                        .toList()
        );
        rabbitTemplate.convertAndSend(exchange, orderPlacedKey, event);

        return OrderResponse.from(saved);
    }

    public void confirmOrder(Long orderId) {
        updateStatus(orderId, "CONFIRMED");
    }

    public void failOrder(Long orderId) {
        updateStatus(orderId, "FAILED");
    }

    private void updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
        order.setStatus(status);
        orderRepository.save(order);
    }

    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(OrderResponse::from)
                .toList();
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .toList();
    }

    public OrderResponse getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));
    }
}

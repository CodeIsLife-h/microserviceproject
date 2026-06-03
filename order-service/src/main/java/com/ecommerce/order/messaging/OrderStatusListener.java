package com.ecommerce.order.messaging;

import com.ecommerce.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderStatusListener {

    private final OrderService orderService;

    public OrderStatusListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = "${rabbitmq.queues.orderConfirmed}")
    public void handleOrderConfirmed(Map<String, Object> event) {
        Long orderId = ((Number) event.get("orderId")).longValue();
        orderService.confirmOrder(orderId);
    }

    @RabbitListener(queues = "${rabbitmq.queues.orderFailed}")
    public void handleOrderFailed(Map<String, Object> event) {
        Long orderId = ((Number) event.get("orderId")).longValue();
        orderService.failOrder(orderId);
    }
}

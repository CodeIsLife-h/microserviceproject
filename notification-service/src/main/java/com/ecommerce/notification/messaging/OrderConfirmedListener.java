package com.ecommerce.notification.messaging;

import com.ecommerce.notification.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OrderConfirmedListener {

    private final EmailService emailService;

    public OrderConfirmedListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${rabbitmq.queues.orderConfirmed}")
    public void handleOrderConfirmed(Map<String, Object> event) {
        Long orderId = ((Number) event.get("orderId")).longValue();
        String customerEmail = (String) event.get("customerEmail");
        List<?> items = (List<?>) event.getOrDefault("items", List.of());
        Object total = event.getOrDefault("total", "N/A");

        emailService.sendOrderConfirmation(orderId, customerEmail, items, total);
    }
}

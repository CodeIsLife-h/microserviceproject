package com.ecommerce.product.messaging;

import com.ecommerce.product.service.ProductService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderEventListener {

    private final ProductService productService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-keys.orderConfirmed}")
    private String orderConfirmedKey;

    @Value("${rabbitmq.routing-keys.orderFailed}")
    private String orderFailedKey;

    public OrderEventListener(ProductService productService, RabbitTemplate rabbitTemplate) {
        this.productService = productService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${rabbitmq.queues.orderPlaced}")
    public void handleOrderPlaced(OrderPlacedMessage message) {
        boolean allDecremented = true;

        for (OrderPlacedMessage.OrderItem item : message.items()) {
            boolean success = productService.decrementStock(item.productId(), item.quantity());
            if (!success) {
                allDecremented = false;
                break;
            }
        }

        String routingKey = allDecremented ? orderConfirmedKey : orderFailedKey;
        Map<String, Object> event = Map.of(
                "orderId", message.orderId(),
                "customerEmail", message.customerEmail(),
                "items", message.items()
        );
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}

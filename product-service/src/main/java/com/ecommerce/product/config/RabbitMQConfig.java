package com.ecommerce.product.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queues.orderPlaced}")
    private String orderPlacedQueue;

    @Value("${rabbitmq.routing-keys.orderPlaced}")
    private String orderPlacedKey;

    @Bean
    public TopicExchange ecommerceExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue orderPlacedQueue() {
        return QueueBuilder.durable(orderPlacedQueue).build();
    }

    @Bean
    public Binding orderPlacedBinding(Queue orderPlacedQueue, TopicExchange ecommerceExchange) {
        return BindingBuilder.bind(orderPlacedQueue).to(ecommerceExchange).with(orderPlacedKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}

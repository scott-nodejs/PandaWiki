package com.chaitin.pandawiki.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {
    
    public static final String DOCUMENT_EXCHANGE = "document.exchange";
    public static final String DOCUMENT_INDEX_QUEUE = "document.index.queue";
    public static final String DOCUMENT_INDEX_ROUTING_KEY = "document.index";
    
    @Bean
    public DirectExchange documentExchange() {
        return new DirectExchange(DOCUMENT_EXCHANGE);
    }
    
    @Bean
    public Queue documentIndexQueue() {
        return new Queue(DOCUMENT_INDEX_QUEUE);
    }
    
    @Bean
    public Binding documentIndexBinding() {
        return BindingBuilder
                .bind(documentIndexQueue())
                .to(documentExchange())
                .with(DOCUMENT_INDEX_ROUTING_KEY);
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
} 
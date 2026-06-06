package com.ecommerce.audit.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para el intercambio y encolado de eventos de auditoría.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue.price-quota-audit}")
    private String auditQueueName;

    @Value("${app.rabbitmq.routing-key.price-changed}")
    private String priceChangedRoutingKey;

    @Value("${app.rabbitmq.routing-key.quota-changed}")
    private String quotaChangedRoutingKey;

    // ===== EXCHANGES =====

    @Bean
    public TopicExchange priceQuotaExchange() {
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    // ===== QUEUES =====

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(auditQueueName)
                .withArgument("x-message-ttl", 600000) // 10 min TTL
                .build();
    }

    // ===== BINDINGS =====

    @Bean
    public Binding priceChangedBinding(Queue auditQueue, TopicExchange priceQuotaExchange) {
        return BindingBuilder.bind(auditQueue).to(priceQuotaExchange).with(priceChangedRoutingKey);
    }

    @Bean
    public Binding quotaChangedBinding(Queue auditQueue, TopicExchange priceQuotaExchange) {
        return BindingBuilder.bind(auditQueue).to(priceQuotaExchange).with(quotaChangedRoutingKey);
    }

    // ===== MESSAGE CONVERTER (JSON con soporte de fechas Java 8+) =====

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);
        return factory;
    }
}

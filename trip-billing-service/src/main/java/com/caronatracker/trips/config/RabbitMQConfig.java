package com.caronatracker.trips.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "carona.events";
    public static final String QUEUE = "trip.registered.queue";
    public static final String ROUTING_KEY = "trip.registered";

    @Bean
    public Queue tripQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public TopicExchange caronaExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding tripBinding(Queue tripQueue, TopicExchange caronaExchange) {
        return BindingBuilder.bind(tripQueue).to(caronaExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}

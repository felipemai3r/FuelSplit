package com.caronatracker.trips.messaging;

import com.caronatracker.trips.config.RabbitMQConfig;
import com.caronatracker.trips.dto.TripEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishTripRegistered(TripEventPayload payload) {
        log.info("Publishing trip event for tripId: {}", payload.getTripId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, payload);
    }
}

package greencity.service;

import greencity.dto.event.EventNotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventNotificationProducer {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.event-notification-exchange}")
    private String exchange;

    @Value("${rabbitmq.event-notification-routing-key}")
    private String routingKey;

    public void sendNotification(EventNotificationDto notification) {
        rabbitTemplate.convertAndSend(exchange, routingKey, notification);
    }
}
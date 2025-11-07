package greencity.service;

import greencity.dto.event.EventNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventNotificationProducer {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.event-notification-exchange}")
    private String exchange;

    @Value("${rabbitmq.event-notification-routing-key}")
    private String routingKey;

    public void sendNotification(EventNotificationDto notification) {

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, notification);

            log.info("Sent event notification: eventId={}, title='{}', type={}",
                notification.getEventId(),
                notification.getEventTitle(),
                notification.getEventType());

        } catch (AmqpException e) {
            log.error("Failed to send event notification: eventId={}. Error: {}",
                notification.getEventId(), e.getMessage());
        }
    }
}
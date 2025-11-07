package greencity.config;

import greencity.dto.event.EventNotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static greencity.config.RabbitConfig.*;

@Slf4j
@Service
public class EventNotificationSender {
    private final RabbitTemplate rabbitTemplate;

    public EventNotificationSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNotification(EventNotificationDto notification) {
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, notification);

        log.info("Sent event notification to RabbitMQ: eventId={}, title='{}', type={}",
            notification.getEventId(),
            notification.getEventTitle(),
            notification.getEventType());
    }
}
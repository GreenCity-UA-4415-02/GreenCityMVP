package greencity.config;

import greencity.dto.event.EventNotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import static greencity.config.RabbitConfig.QUEUE_NAME;

@Slf4j
@Service
public class EventNotificationListener {
    @RabbitListener(queues = QUEUE_NAME)
    public void handleMessage(EventNotificationDto notification) {
        log.info("Received event notification from RabbitMQ: eventId={}, title='{}', type={}",
            notification.getEventId(),
            notification.getEventTitle(),
            notification.getEventType());
    }
}
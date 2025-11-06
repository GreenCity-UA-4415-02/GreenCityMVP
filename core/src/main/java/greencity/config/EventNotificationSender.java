package greencity.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static greencity.config.RabbitConfig.*;

@Service
public class EventNotificationSender {
    private final RabbitTemplate rabbitTemplate;

    public EventNotificationSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNotification(String message) {
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, message);
        System.out.println("Відправлено повідомлення у RabbitMQ: " + message);
    }
}

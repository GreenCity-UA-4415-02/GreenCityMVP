package greencity.config;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import static greencity.config.RabbitConfig.QUEUE_NAME;

@Service
public class EventNotificationListener {

    @RabbitListener(queues = QUEUE_NAME)
    public void handleMessage(String message) {
        System.out.println(" Отримано повідомлення від RabbitMQ: " + message);
    }
}

package greencity.config;

import greencity.dto.event.EventUpdatePayload;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class GraphQLSubscriptionConfig {

    /**
     * Створює єдиний, багатоадресний "транслятор" (Publisher/Sink) для оновлень подій.
     * Всі підписники будуть отримувати дані з цього джерела.
     * .multicast() - дозволяє мати багато підписників.
     * .onBackpressureBuffer() - буферизує повідомлення, якщо клієнти не встигають їх обробляти.
     * @return a thread-safe Sink for event updates.
     */
    @Bean
    public Sinks.Many<EventUpdatePayload> eventUpdateSink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }
}
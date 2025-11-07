package greencity.controller;

import greencity.dto.event.EventUpdatePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Controller
@RequiredArgsConstructor
public class EventSubscriptionController {

    private final Sinks.Many<EventUpdatePayload> eventUpdateSink;

    /**
     * Цей метод обробляє підписку "eventUpdates", визначену в GraphQL-схемі. Він
     * повертає потік (Flux), на який підписується клієнт.
     *
     * @return a Publisher (Flux) of event updates.
     */
    @SubscriptionMapping
    public Flux<EventUpdatePayload> eventUpdates() {
        return eventUpdateSink.asFlux();
    }
}
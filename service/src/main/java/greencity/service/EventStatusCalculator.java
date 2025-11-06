package greencity.service;

import greencity.entity.EventDateLocation;
import greencity.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public final class EventStatusCalculator {
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class EventStatusResult {
        private EventStatus status;
        private LocalDateTime nearestStart;
        private LocalDateTime nearestFinish;
    }

    public static EventStatusResult computeStatus(List<EventDateLocation> dateLocations, LocalDateTime now) {
        if (dateLocations == null || dateLocations.isEmpty()) {
            return EventStatusResult.builder()
                    .status(EventStatus.PASSED)
                    .nearestStart(null)
                    .nearestFinish(null)
                    .build();
        }

        Optional<EventDateLocation> liveOccurrence = dateLocations.stream()
                .filter(loc -> !now.isBefore(loc.getStartDate()) && !now.isAfter(loc.getFinishDate()))
                .findFirst();

        if (liveOccurrence.isPresent()) {
            EventDateLocation live = liveOccurrence.get();
            return EventStatusResult.builder()
                    .status(EventStatus.LIVE)
                    .nearestStart(live.getStartDate())
                    .nearestFinish(live.getFinishDate())
                    .build();
        }

        Optional<EventDateLocation> nextOccurrence = dateLocations.stream()
                .filter(loc -> now.isBefore(loc.getStartDate()))
                .min((a, b) -> a.getStartDate().compareTo(b.getStartDate()));

        if (nextOccurrence.isPresent()) {
            EventDateLocation next = nextOccurrence.get();
            return EventStatusResult.builder()
                    .status(EventStatus.UPCOMING)
                    .nearestStart(next.getStartDate())
                    .nearestFinish(next.getFinishDate())
                    .build();
        }

        return EventStatusResult.builder()
                .status(EventStatus.PASSED)
                .nearestStart(null)
                .nearestFinish(null)
                .build();
    }
}

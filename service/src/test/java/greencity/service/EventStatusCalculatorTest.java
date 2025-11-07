package greencity.service;

import greencity.entity.Event;
import greencity.entity.EventDateLocation;
import greencity.enums.EventStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventStatusCalculatorTest {

    @Test
    void computeStatus_WithLiveOccurrence_ReturnsLive() {
        LocalDateTime now = LocalDateTime.now();
        List<EventDateLocation> dateLocations = createDateLocations(
            now.minusHours(1), now.plusHours(1));

        EventStatusCalculator.EventStatusResult result =
            EventStatusCalculator.computeStatus(dateLocations, now);

        assertEquals(EventStatus.LIVE, result.getStatus());
        assertNotNull(result.getNearestStart());
        assertNotNull(result.getNearestFinish());
        assertEquals(dateLocations.get(0).getStartDate(), result.getNearestStart());
        assertEquals(dateLocations.get(0).getFinishDate(), result.getNearestFinish());
    }

    @Test
    void computeStatus_WithUpcomingOccurrence_ReturnsUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        List<EventDateLocation> dateLocations = createDateLocations(
            now.plusDays(1), now.plusDays(1).plusHours(2));

        EventStatusCalculator.EventStatusResult result =
            EventStatusCalculator.computeStatus(dateLocations, now);

        assertEquals(EventStatus.UPCOMING, result.getStatus());
        assertNotNull(result.getNearestStart());
        assertNotNull(result.getNearestFinish());
        assertEquals(dateLocations.get(0).getStartDate(), result.getNearestStart());
        assertEquals(dateLocations.get(0).getFinishDate(), result.getNearestFinish());
    }

    @Test
    void computeStatus_WithPassedOccurrence_ReturnsPassed() {
        LocalDateTime now = LocalDateTime.now();
        List<EventDateLocation> dateLocations = createDateLocations(
            now.minusDays(2), now.minusDays(1));

        EventStatusCalculator.EventStatusResult result =
            EventStatusCalculator.computeStatus(dateLocations, now);

        assertEquals(EventStatus.PASSED, result.getStatus());
        assertNull(result.getNearestStart());
        assertNull(result.getNearestFinish());
    }

    @Test
    void computeStatus_WithMultipleOccurrences_FirstIsLive_ReturnsLive() {
        LocalDateTime now = LocalDateTime.now();
        Event event = Event.builder().id(1L).build();

        List<EventDateLocation> dateLocations = Arrays.asList(
            createDateLocation(event, 1L, now.minusHours(1), now.plusHours(1)),
            createDateLocation(event, 2L, now.plusDays(1), now.plusDays(1).plusHours(2)));

        EventStatusCalculator.EventStatusResult result =
            EventStatusCalculator.computeStatus(dateLocations, now);

        assertEquals(EventStatus.LIVE, result.getStatus());
        assertEquals(dateLocations.get(0).getStartDate(), result.getNearestStart());
        assertEquals(dateLocations.get(0).getFinishDate(), result.getNearestFinish());
    }

    @Test
    void computeStatus_WithMultipleOccurrences_SecondIsLive_ReturnsLive() {
        LocalDateTime now = LocalDateTime.now();
        Event event = Event.builder().id(1L).build();

        List<EventDateLocation> dateLocations = Arrays.asList(
            createDateLocation(event, 1L, now.minusDays(1), now.minusDays(1).plusHours(2)),
            createDateLocation(event, 2L, now.minusHours(1), now.plusHours(1)),
            createDateLocation(event, 3L, now.plusDays(1), now.plusDays(1).plusHours(2)));

        EventStatusCalculator.EventStatusResult result =
            EventStatusCalculator.computeStatus(dateLocations, now);

        assertEquals(EventStatus.LIVE, result.getStatus());
        assertEquals(dateLocations.get(1).getStartDate(), result.getNearestStart());
        assertEquals(dateLocations.get(1).getFinishDate(), result.getNearestFinish());
    }

    @Test
    void computeStatus_WithMultipleUpcomingOccurrences_ReturnsEarliest() {
        LocalDateTime now = LocalDateTime.now();
        Event event = Event.builder().id(1L).build();

        List<EventDateLocation> dateLocations = Arrays.asList(
            createDateLocation(event, 1L, now.plusDays(3), now.plusDays(3).plusHours(2)),
            createDateLocation(event, 2L, now.plusDays(1), now.plusDays(1).plusHours(2)),
            createDateLocation(event, 3L, now.plusDays(5), now.plusDays(5).plusHours(2)));

        EventStatusCalculator.EventStatusResult result =
            EventStatusCalculator.computeStatus(dateLocations, now);

        assertEquals(EventStatus.UPCOMING, result.getStatus());
        assertEquals(dateLocations.get(1).getStartDate(), result.getNearestStart());
        assertEquals(dateLocations.get(1).getFinishDate(), result.getNearestFinish());
    }

    @Test
    void computeStatus_WithMixedOccurrences_HasLive_ReturnsLive() {
        LocalDateTime now = LocalDateTime.now();
        Event event = Event.builder().id(1L).build();

        List<EventDateLocation> dateLocations = Arrays.asList(
            createDateLocation(event, 1L, now.minusDays(2), now.minusDays(1)), // PASSED
            createDateLocation(event, 2L, now.minusHours(1), now.plusHours(1)), // LIVE
            createDateLocation(event, 3L, now.plusDays(1), now.plusDays(1).plusHours(2)) // UPCOMING
        );

        EventStatusCalculator.EventStatusResult result =
            EventStatusCalculator.computeStatus(dateLocations, now);

        assertEquals(EventStatus.LIVE, result.getStatus());
        assertEquals(dateLocations.get(1).getStartDate(), result.getNearestStart());
    }

    @Test
    void computeStatus_WithEmptyList_ReturnsPassed() {
        LocalDateTime now = LocalDateTime.now();
        List<EventDateLocation> dateLocations = Collections.emptyList();

        EventStatusCalculator.EventStatusResult result =
            EventStatusCalculator.computeStatus(dateLocations, now);

        assertEquals(EventStatus.PASSED, result.getStatus());
        assertNull(result.getNearestStart());
        assertNull(result.getNearestFinish());
    }

    @Test
    void computeStatus_WithNullList_ReturnsPassed() {
        LocalDateTime now = LocalDateTime.now();

        EventStatusCalculator.EventStatusResult result =
            EventStatusCalculator.computeStatus(null, now);

        assertEquals(EventStatus.PASSED, result.getStatus());
        assertNull(result.getNearestStart());
        assertNull(result.getNearestFinish());
    }

    @Test
    void computeStatus_AtExactStartTime_ReturnsLive() {
        LocalDateTime now = LocalDateTime.now();
        List<EventDateLocation> dateLocations = createDateLocations(
            now, now.plusHours(2));

        EventStatusCalculator.EventStatusResult result =
            EventStatusCalculator.computeStatus(dateLocations, now);

        assertEquals(EventStatus.LIVE, result.getStatus());
    }

    @Test
    void computeStatus_AtExactFinishTime_ReturnsLive() {
        LocalDateTime now = LocalDateTime.now();
        List<EventDateLocation> dateLocations = createDateLocations(
            now.minusHours(2), now);

        EventStatusCalculator.EventStatusResult result =
            EventStatusCalculator.computeStatus(dateLocations, now);

        assertEquals(EventStatus.LIVE, result.getStatus());
    }

    @Test
    void computeStatus_OneSecondBeforeStart_ReturnsUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        List<EventDateLocation> dateLocations = createDateLocations(
            now.plusSeconds(1), now.plusHours(2));

        EventStatusCalculator.EventStatusResult result =
            EventStatusCalculator.computeStatus(dateLocations, now);

        assertEquals(EventStatus.UPCOMING, result.getStatus());
    }

    @Test
    void computeStatus_OneSecondAfterFinish_ReturnsPassed() {
        LocalDateTime now = LocalDateTime.now();
        List<EventDateLocation> dateLocations = createDateLocations(
            now.minusHours(2), now.minusSeconds(1));

        EventStatusCalculator.EventStatusResult result =
            EventStatusCalculator.computeStatus(dateLocations, now);

        assertEquals(EventStatus.PASSED, result.getStatus());
    }

    private List<EventDateLocation> createDateLocations(LocalDateTime start, LocalDateTime finish) {
        Event event = Event.builder().id(1L).build();
        return Collections.singletonList(
            createDateLocation(event, 1L, start, finish));
    }

    private EventDateLocation createDateLocation(Event event, Long id,
        LocalDateTime start, LocalDateTime finish) {
        return EventDateLocation.builder()
            .id(id)
            .event(event)
            .startDate(start)
            .finishDate(finish)
            .latitude(BigDecimal.valueOf(50.4501))
            .longitude(BigDecimal.valueOf(30.5234))
            .build();
    }
}

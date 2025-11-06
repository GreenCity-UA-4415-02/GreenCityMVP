package greencity.repository;

import greencity.entity.EventDateLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface EventDateLocationRepo extends JpaRepository<EventDateLocation, Long>,
        JpaSpecificationExecutor<EventDateLocation> {
    @Query("SELECT dl FROM EventDateLocation dl WHERE dl.event.id = :eventId ORDER BY dl.startDate ASC")
    List<EventDateLocation> findByEventIdOrderByStartDate(@Param("eventId") Long eventId);

    @Query("SELECT CASE WHEN COUNT(dl) > 0 THEN true ELSE false END " +
            "FROM EventDateLocation dl " +
            "WHERE dl.event.id = :eventId " +
            "AND :now >= dl.startDate AND :now <= dl.finishDate")
    boolean hasLiveOccurrence(@Param("eventId") Long eventId, @Param("now") OffsetDateTime now);

    @Query("SELECT MIN(dl.startDate) FROM EventDateLocation dl " +
            "WHERE dl.event.id = :eventId AND dl.startDate > :now")
    OffsetDateTime findEarliestFutureStartDate(@Param("eventId") Long eventId, @Param("now") OffsetDateTime now);

    @Query("SELECT MAX(dl.finishDate) FROM EventDateLocation dl WHERE dl.event.id = :eventId")
    OffsetDateTime findLatestFinishDate(@Param("eventId") Long eventId);

    @Query("SELECT dl FROM EventDateLocation dl " +
            "WHERE dl.event.id = :eventId AND dl.startDate > :now " +
            "ORDER BY dl.startDate ASC")
    List<EventDateLocation> findEarliestFutureOccurrence(@Param("eventId") Long eventId, @Param("now") OffsetDateTime now);

    @Query("SELECT dl FROM EventDateLocation dl " +
            "WHERE dl.event.id = :eventId " +
            "AND :now >= dl.startDate AND :now <= dl.finishDate " +
            "ORDER BY dl.startDate ASC")
    List<EventDateLocation> findLiveOccurrence(@Param("eventId") Long eventId, @Param("now") OffsetDateTime now);
}
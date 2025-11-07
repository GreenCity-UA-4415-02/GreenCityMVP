package greencity.repository;

import greencity.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;
import java.util.List;

@Repository
public interface EventRepo extends JpaRepository<Event, Long> {
    @Query("SELECT e FROM Event e WHERE e.isOpen = TRUE")
    List<Event> findAllOpenEvents();

    @EntityGraph(attributePaths = {"images", "dateTimeLocations", "organizer", "tags"})
    Optional<Event> findById(Long id);

    @Query(
        value = """
            SELECT e FROM Event e
            WHERE e.organizer.id = :organizerId
            ORDER BY (SELECT MIN(l.startDate) FROM e.dateTimeLocations l) ASC
            """,
        countQuery = """
               SELECT COUNT(e) FROM Event e
               WHERE e.organizer.id = :organizerId
            """)
    Page<Event> findByOrganizerIdOrderByNearestStart(@Param("organizerId") Long organizerId, Pageable pageable);

    @Query(
        value = """
            SELECT e FROM Event e
            WHERE e.id IN (
                SELECT e2.id FROM Event e2 WHERE e2.organizer.id = :userId
                UNION
                SELECT ea.eventId FROM EventAttender ea WHERE ea.userId = :userId
            )
            ORDER BY (SELECT MIN(l.startDate) FROM e.dateTimeLocations l) ASC
            """,
        countQuery = """
            SELECT COUNT(e.id) FROM Event e
            WHERE e.id IN (
                SELECT e2.id FROM Event e2 WHERE e2.organizer.id = :userId
                UNION
                SELECT ea.eventId FROM EventAttender ea WHERE ea.userId = :userId
            )
            """)
    Page<Event> findRelatedEventsByUserId(@Param("userId") Long userId, Pageable pageable);
}

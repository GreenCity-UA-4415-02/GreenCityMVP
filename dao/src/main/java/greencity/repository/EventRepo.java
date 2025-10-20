package greencity.repository;

import greencity.entity.Event;
import greencity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}

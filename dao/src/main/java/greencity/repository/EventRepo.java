package greencity.repository;

import greencity.entity.Event;
import greencity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepo extends JpaRepository<Event, Long> {
    @Query("SELECT e FROM Event e WHERE e.isOpen = TRUE")
    List<Event> findAllOpenEvents();
}

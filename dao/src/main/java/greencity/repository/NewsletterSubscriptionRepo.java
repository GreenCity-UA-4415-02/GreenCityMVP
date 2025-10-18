package greencity.repository;

import greencity.entity.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link NewsletterSubscription} entities.
 * Extends {@link JpaRepository} to provide standard CRUD and pagination functionalities.
 */
public interface NewsletterSubscriptionRepo extends JpaRepository<NewsletterSubscription, String> {
    /**
     * Finds a newsletter subscription record by the user's email address.
     *
     * @param email The email address to search for.
     * @return An {@link Optional} containing the {@link NewsletterSubscription} if found,
     * or an empty Optional if no record matches the email.
     */
    Optional<NewsletterSubscription> findByEmail(String email);
}

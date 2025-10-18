package greencity.repository;

import greencity.entity.NewsletterSubscription;
import greencity.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsletterSubscriptionRepo extends JpaRepository<NewsletterSubscription, String> {
    boolean existsByEmail(String email);
    Optional<NewsletterSubscription> findByEmail(String email);
    SubscriptionStatus findByEmailAndStatus(String email, SubscriptionStatus status);
}

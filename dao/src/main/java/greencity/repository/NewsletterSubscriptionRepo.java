package greencity.repository;

import greencity.entity.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriptionRepo extends JpaRepository<NewsletterSubscription, String> {
    boolean existsByEmail(String email);
}

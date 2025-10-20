package greencity.service;

import greencity.dto.subscription.SubscribeResultDto;
import greencity.dto.subscription.SubscriptionDto;
import greencity.entity.NewsletterSubscription;
import greencity.enums.SubscriptionSource;
import greencity.enums.SubscriptionStatus;
import greencity.repository.NewsletterSubscriptionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZonedDateTime;

/**
 * Release interface {@link NewsletterService}.
 */
@Service
@RequiredArgsConstructor
public class NewsletterServiceImpl implements NewsletterService {
    private final NewsletterSubscriptionRepo newsletterSubscriptionRepo;

    @Override
    @Transactional
    public SubscribeResultDto subscribe(SubscriptionDto subscriptionDto) {
        String email = subscriptionDto.getEmail();

        if (newsletterSubscriptionRepo.existsByEmail(email)) {
            return SubscribeResultDto.builder()
                .ok(true)
                .alreadySubscribed(true)
                .build();
        }

        SubscriptionSource sourceEnum = SubscriptionSource.valueOf(subscriptionDto.getSource().toUpperCase());
        NewsletterSubscription subscriptionEntity = NewsletterSubscription.builder()
            .email(email)
            .source(sourceEnum)
            .status(SubscriptionStatus.SUBSCRIBED)
            .createdAt(ZonedDateTime.now())
            .build();

        newsletterSubscriptionRepo.save(subscriptionEntity);
        return SubscribeResultDto.builder()
            .ok(true)
            .alreadySubscribed(false)
            .build();
    }
}

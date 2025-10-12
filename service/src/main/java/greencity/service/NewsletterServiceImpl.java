package greencity.service;

import greencity.dto.subscription.SubscriptionDto;
import greencity.dto.subscription.SubscriptionResponseDto;
import greencity.entity.NewsletterSubscription;
import greencity.enums.SubscriptionSource;
import greencity.enums.SubscriptionStatus;
import greencity.exception.exceptions.EmailAlreadySignedUpException;
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
    public SubscriptionResponseDto subscribe(SubscriptionDto subscriptionDto) {
        String email = subscriptionDto.getEmail();

        if (newsletterSubscriptionRepo.existsByEmail(email)) {
            throw new EmailAlreadySignedUpException("User with email: " + email + " is already subscribed to the newsletter.");
        }

        NewsletterSubscription subscriptionEntity = NewsletterSubscription.builder()
                                                                          .email(email)
                                                                          .source(SubscriptionSource.valueOf(subscriptionDto
                                                                                  .getSource()
                                                                                  .toUpperCase()))
                                                                          .status(SubscriptionStatus.SUBSCRIBED)
                                                                          .createdAt(ZonedDateTime.now())
                                                                          .build();

        NewsletterSubscription savedEntity = newsletterSubscriptionRepo.save(subscriptionEntity);

        return SubscriptionResponseDto.builder()
                                      .email(savedEntity.getEmail())
                                      .subscribedAt(savedEntity.getCreatedAt())
                                      .build();
    }
}

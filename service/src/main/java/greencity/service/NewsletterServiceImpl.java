package greencity.service;

import greencity.dto.subscription.SubscribeResultDto;
import greencity.dto.subscription.SubscriptionDto;
import greencity.dto.subscription.UnsubscriptionResultDto;
import greencity.entity.NewsletterSubscription;
import greencity.enums.SubscriptionSource;
import greencity.enums.SubscriptionStatus;
import greencity.repository.NewsletterSubscriptionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Implementation of the {@link NewsletterService} interface.
 * Provides business logic for managing newsletter subscriptions and unsubscriptions.
 */
@Service
@RequiredArgsConstructor
public class NewsletterServiceImpl implements NewsletterService {
    private final NewsletterSubscriptionRepo newsletterSubscriptionRepo;

    /**
     * Subscribes a user to the newsletter based on the provided email and source.
     * The operation is **idempotent**:
     * 1. If the email is not found, a new subscription is created with status {@code SUBSCRIBED}.
     * 2. If the email is found and status is already {@code SUBSCRIBED}, returns a result indicating it was already subscribed.
     * 3. If the email is found and status is {@code UNSUBSCRIBED}, the status is changed to {@code SUBSCRIBED}, and the {@code source} and {@code updatedAt} fields are updated.
     *
     * @param subscriptionDto DTO containing the user's email address and subscription source.
     * @return {@link SubscribeResultDto} with the status of the subscription attempt.
     */
    @Override
    @Transactional
    public SubscribeResultDto subscribe(SubscriptionDto subscriptionDto) {
        String email = subscriptionDto.getEmail();
        SubscriptionSource sourceEnum = SubscriptionSource.valueOf(subscriptionDto.getSource().toUpperCase());

        Optional<NewsletterSubscription> existingSubscription = newsletterSubscriptionRepo.findByEmail(email);

        if (existingSubscription.isPresent()) {
            NewsletterSubscription subscription = existingSubscription.get();
            if (SubscriptionStatus.SUBSCRIBED.equals(subscription.getStatus())) {
                return SubscribeResultDto.builder()
                                         .ok(true)
                                         .alreadySubscribed(true)
                                         .build();
            } else if (SubscriptionStatus.UNSUBSCRIBED.equals(subscription.getStatus())) {
                subscription.setStatus(SubscriptionStatus.SUBSCRIBED);
                subscription.setSource(sourceEnum);
                subscription.setUpdatedAt(ZonedDateTime.now());
                newsletterSubscriptionRepo.save(subscription);

                return SubscribeResultDto.builder()
                                         .ok(true)
                                         .alreadySubscribed(false)
                                         .build();
            }
        }

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

    /**
     * Unsubscribes a user from the newsletter based on the provided email.
     * The operation is **idempotent** and always returns a successful response (HTTP 200) confirming the final status is 'unsubscribed'.
     *
     * 1. If the email is found and status is {@code SUBSCRIBED}, the status is changed to {@code UNSUBSCRIBED}.
     * 2. If the email is found and status is already {@code UNSUBSCRIBED}, no change is made.
     * 3. If the email is not found, no change is made.
     *
     * @param email The email address to unsubscribe.
     * @return {@link UnsubscriptionResultDto} indicating the final 'unsubscribed' status and whether a record existed.
     */
    @Override
    @Transactional
    public UnsubscriptionResultDto unsubscribe(String email) {
        Optional<NewsletterSubscription> subscriptionOptional = newsletterSubscriptionRepo.findByEmail(email);
        if (subscriptionOptional.isPresent()) {
            NewsletterSubscription subscription = subscriptionOptional.get();
            if (!SubscriptionStatus.UNSUBSCRIBED.equals(subscription.getStatus())) {
                subscription.setStatus(SubscriptionStatus.UNSUBSCRIBED);
                subscription.setUpdatedAt(ZonedDateTime.now());
                newsletterSubscriptionRepo.save(subscription);

                return UnsubscriptionResultDto.builder()
                                         .ok(true)
                                         .alreadySubscribed(true)
                                         .status("unsubscribed")
                                         .build();
            }
            return UnsubscriptionResultDto.builder()
                                          .ok(true)
                                          .alreadySubscribed(true)
                                          .status("unsubscribed")
                                          .build();
        }
        return UnsubscriptionResultDto.builder()
                                 .ok(true)
                                 .alreadySubscribed(false)
                                 .status("unsubscribed")
                                 .build();
    }
}

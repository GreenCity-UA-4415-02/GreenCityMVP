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

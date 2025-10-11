package greencity.service;

import greencity.dto.subscription.SubscriptionDto;
import greencity.dto.subscription.SubscriptionResponseDto;

public interface NewsletterService {
    SubscriptionResponseDto subscribe(SubscriptionDto subscriptionDto);

}

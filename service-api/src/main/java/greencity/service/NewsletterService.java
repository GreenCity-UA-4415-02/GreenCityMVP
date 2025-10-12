package greencity.service;

import greencity.dto.subscription.SubscribeResultDto;
import greencity.dto.subscription.SubscriptionDto;

public interface NewsletterService {
    SubscribeResultDto subscribe(SubscriptionDto subscriptionDto);

}

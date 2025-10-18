package greencity.service;

import greencity.dto.subscription.SubscribeResultDto;
import greencity.dto.subscription.SubscriptionDto;
import greencity.dto.subscription.UnsubscriptionResultDto;

public interface NewsletterService {
    SubscribeResultDto subscribe(SubscriptionDto subscriptionDto);
    UnsubscriptionResultDto unsubscribe(String email);
}

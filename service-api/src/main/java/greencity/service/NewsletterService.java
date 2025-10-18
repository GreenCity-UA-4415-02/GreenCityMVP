package greencity.service;

import greencity.dto.subscription.SubscribeResultDto;
import greencity.dto.subscription.SubscriptionDto;
import greencity.dto.subscription.UnsubscriptionResultDto;

/**
 * Provides business logic for managing user subscriptions to the newsletter.
 * This includes subscribing new users and handling unsubscription requests.
 */
public interface NewsletterService {
    /**
     * Handles the subscription request for a user.
     * The operation is idempotent, meaning if the user is already subscribed,
     * it will confirm the existing subscription without making changes.
     *
     * @param subscriptionDto DTO containing the user's email and subscription source.
     * @return {@link SubscribeResultDto} with the result status of the operation.
     */
    SubscribeResultDto subscribe(SubscriptionDto subscriptionDto);
    /**
     * Handles the unsubscription request for a user.
     * The operation is idempotent, meaning if the user is already unsubscribed
     * or the email is not found, it will return a success status confirming
     * the final status is 'unsubscribed'.
     *
     * @param email The email address to unsubscribe.
     * @return {@link UnsubscriptionResultDto} with the result status of the operation.
     */
    UnsubscriptionResultDto unsubscribe(String email);
}

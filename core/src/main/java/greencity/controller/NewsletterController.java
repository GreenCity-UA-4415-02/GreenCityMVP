package greencity.controller;

import greencity.dto.subscription.SubscribeResultDto;
import greencity.dto.subscription.SubscriptionDto;
import greencity.dto.subscription.UnsubscriptionDto;
import greencity.dto.subscription.UnsubscriptionResultDto;
import greencity.service.NewsletterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing newsletter subscriptions.
 */
@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {
    private final NewsletterService newsletterService;

    /**
     * Processes a subscription request for the newsletter. The operation is
     * idempotent: if the user is already subscribed, it confirms their status
     * without change.
     *
     * @param subscriptionDto DTO containing the user's email address and
     *                        subscription source.
     * @return {@link ResponseEntity} with {@link SubscribeResultDto} and HTTP
     *         status 200 (OK).
     */
    @Operation(summary = "Subscribe a user to the newsletter",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "Successful subscription, body indicates if it's new or already subscribed"),
            @ApiResponse(responseCode = "400", description = "Incorrect email or source format")
        })
    @PostMapping("/subscribe")
    public ResponseEntity<SubscribeResultDto> subscribe(@Valid @RequestBody SubscriptionDto subscriptionDto) {
        SubscribeResultDto result = newsletterService.subscribe(subscriptionDto);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /**
     * Processes an unsubscription request for the newsletter. The operation is
     * idempotent: if the user is already unsubscribed or the email does not exist,
     * it returns a success status confirming the user is not subscribed.
     *
     * @param unsubscriptionDto DTO containing the user's email address.
     * @return {@link ResponseEntity} with {@link UnsubscriptionResultDto} and HTTP
     *         status 200 (OK).
     */
    @Operation(summary = "Unsubscribe a user to the newsletter",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful unsubscription"),
            @ApiResponse(responseCode = "400", description = "Incorrect email or source format")
        })
    @PostMapping("/unsubscribe")
    public ResponseEntity<UnsubscriptionResultDto> unsubscribe(
        @Valid @RequestBody UnsubscriptionDto unsubscriptionDto) {
        UnsubscriptionResultDto result = newsletterService.unsubscribe(unsubscriptionDto.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}

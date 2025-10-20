package greencity.controller;

import greencity.dto.subscription.SubscribeResultDto;
import greencity.dto.subscription.SubscriptionDto;
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

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {
    private final NewsletterService newsletterService;

    /**
     * Processes subscription request.
     *
     * @param subscriptionDto DTO, that have email address.
     * @return 200 OK, if subscribe successful.
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
}

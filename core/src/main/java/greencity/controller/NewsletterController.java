package greencity.controller;

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
     * 409 CONFLICT, if the mail is already signed (processed at the level
     * ControllerAdvice).
     */
    @Operation(summary = "Subscribe a user to the newsletter",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful subscription"),
                    @ApiResponse(responseCode = "400", description = "Incorrect email format"),
                    @ApiResponse(responseCode = "409", description = "The user is already signed in")
            })
    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@Valid @RequestBody SubscriptionDto subscriptionDto) {
        newsletterService.subscribe(subscriptionDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

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
@RequestMapping("/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;

    /**
     * Обробляє запит на підписку.
     *
     * @param subscriptionDto DTO, що містить електронну пошту.
     * @return 200 OK, якщо підписка успішна.
     * 409 CONFLICT, якщо пошта вже підписана (обробляється на рівні
     * ControllerAdvice).
     */
    @Operation(summary = "Підписати користувача на розсилку новин",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успішна підписка"),
                    @ApiResponse(responseCode = "400", description = "Некоректний формат email"),
                    @ApiResponse(responseCode = "409", description = "Користувач вже підписаний")
            })
    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@Valid @RequestBody SubscriptionDto subscriptionDto) {
        newsletterService.subscribe(subscriptionDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

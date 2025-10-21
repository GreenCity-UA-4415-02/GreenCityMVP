package greencity.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the result of a subscription attempt for the POST
 * /api/newsletter/subscribe endpoint. Structure: { "ok": true,
 * "alreadySubscribed": false/true }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscribeResultDto {
    /**
     * Indicates if the operation was successful (always true for HTTP 200/201).
     */
    @Schema(description = "Indicates if the operation was successful", example = "true")
    private boolean ok;

    /**
     * Indicates if the email was already subscribed before this request.
     */
    @Schema(description = "Indicates if the email was already subscribed before this request", example = "false")
    private boolean alreadySubscribed;
}
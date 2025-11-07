package greencity.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnsubscriptionResultDto {
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

    @Schema(description = "Indicates if the subscription was successful", example = "true")
    private String status;
}

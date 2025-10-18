package greencity.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnsubscriptionDto {
    @NotBlank(message = "Email must not be blank.")
    @Email(message = "Email must be a valid email format.")
    @Size(min = 6, max = 255, message = "Email length must be between 6 and 255 characters.")
    @Schema(description = "Email for subscription", example = "test@example.com")
    private String email;
}

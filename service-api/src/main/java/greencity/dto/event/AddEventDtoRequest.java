package greencity.dto.event;

import greencity.dto.tag.TagUaEnDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class AddEventDtoRequest {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Boolean open;

    @NotEmpty
    private List<TagUaEnDto> tags;

    @NotEmpty
    private List<DateLocationDto> datesLocations;
}

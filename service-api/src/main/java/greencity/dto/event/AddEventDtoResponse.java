package greencity.dto.event;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class AddEventDtoResponse {
    private Long id;
    private String title;
    private String description;
    private Boolean open;
    private List<String> tagNames;
    private List<DateLocationDto> datesLocations;
    private List<String> images;
}

package greencity.dto.event;

import greencity.enums.EventStatus;
import lombok.*;
import java.time.ZonedDateTime;
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
    private EventStatus status;
    private ZonedDateTime nearestStart;
    private ZonedDateTime nearestFinish;
}

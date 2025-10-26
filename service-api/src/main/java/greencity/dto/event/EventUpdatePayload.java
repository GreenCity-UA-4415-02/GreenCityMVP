package greencity.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventUpdatePayload {
    private Long id;
    private String title;
    private EventActionType eventType;
}
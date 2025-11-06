package greencity.dto.event;

import greencity.enums.EventStatus;
import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class EventPreviewDto {
    private Long id;
    private String title;
    private String titleImage;
    private EventStatus status;
    private LocalDateTime nearestStart;
    private LocalDateTime nearestFinish;
    private EventTypesDto types;
    private Double distance;
    private String visibility;
    private boolean canCancelJoin;
    private boolean canEdit;
    private boolean isFavourite;
    private boolean isSubscribed;
    private boolean isOrganizer;
}

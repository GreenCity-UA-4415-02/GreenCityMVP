package greencity.dto.event;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class EventDto {
    private Long id;
    private String title;
    private String description;
    private Boolean is_open;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}

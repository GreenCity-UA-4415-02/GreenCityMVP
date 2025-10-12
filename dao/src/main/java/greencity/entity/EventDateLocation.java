package greencity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_date_location")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class EventDateLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startDate;
    private LocalDateTime finishDate;

    private String address;

    private Double latitude;
    private Double longitude;
    private String onlineLink;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
}

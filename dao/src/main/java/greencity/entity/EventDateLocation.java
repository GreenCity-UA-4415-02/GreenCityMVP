package greencity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events_dates_locations")
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

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "finish_date", nullable = false)
    private LocalDateTime finishDate;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "online_link")
    private String onlineLink;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}

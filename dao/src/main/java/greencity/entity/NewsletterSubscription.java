package greencity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.ZonedDateTime;

@Entity
@Table(name = "newsletter_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsletterSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, unique = true, length = 255, name = "email")
    private String email;

    @Column(name = "subscribed_at", nullable = false)
    private ZonedDateTime subscribedAt;
}

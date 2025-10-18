package com.thirikkale.scheduledrideservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.thirikkale.scheduledrideservice.model.enums.ScheduledRideStatus;

@Document(collection = "scheduled_rides")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ScheduledRide {
    @Id
    private UUID id;

    private UUID riderId;
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;

    private String dropoffAddress;
    private Double dropoffLatitude;
    private Double dropoffLongitude;

    private Integer passengers;
    private Boolean shared; // true for shared ride request

    @Indexed
    private LocalDateTime scheduledTime;
    // store enum as String automatically by Spring Data MongoDB
    private ScheduledRideStatus status;

    private UUID sharedGroupId; // nullable; populated when grouped

    // optional precomputed estimates
    private BigDecimal estimatedFare;
    private Double estimatedDistanceKm;
    private Integer estimatedDurationMin;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
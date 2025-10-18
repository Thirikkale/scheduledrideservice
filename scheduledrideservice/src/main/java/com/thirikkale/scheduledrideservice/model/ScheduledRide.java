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
    private String id;

    private String riderId;
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;

    private String dropoffAddress;
    private Double dropoffLatitude;
    private Double dropoffLongitude;

    private Integer passengers;
    private Boolean isSharedRide; // true for shared ride request

    @Indexed
    private LocalDateTime scheduledTime;
    private ScheduledRideStatus status;

    private String sharedGroupId; // nullable; populated when grouped

    // Ride options and preferences for dispatch
    private String rideType;       // enum name as String
    private String vehicleType;    // enum name as String
    private Double distanceKm;
    private Integer waitingTimeMin;
    private Boolean isWomenOnly;
    // Driver assigned to the scheduled ride
    private String driverId;
    private Double maxFare;
    private String specialRequests;

    // optional precomputed estimates
    private BigDecimal estimatedFare;
    private Double estimatedDistanceKm;
    private Integer estimatedDurationMin;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
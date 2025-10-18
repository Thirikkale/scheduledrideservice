package com.thirikkale.scheduledrideservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduledRideCreateRequestDto {
    @NotNull private String riderId;
    @NotBlank private String pickupAddress;
    @NotNull private Double pickupLatitude;
    @NotNull private Double pickupLongitude;
    @NotBlank private String dropoffAddress;
    @NotNull private Double dropoffLatitude;
    @NotNull private Double dropoffLongitude;
    @NotNull private LocalDateTime scheduledTime;
    @NotNull private Integer passengers;
    @NotNull private Boolean shared;

    // Ride options and preferences
    @NotBlank private String rideType;     // must match rideservice RideType enum name
    @NotBlank private String vehicleType;  // must match rideservice VehicleType enum name
    @Positive private Double distanceKm;         // optional, server computes if null
    @PositiveOrZero private Integer waitingTimeMin;
    private Boolean womenOnly;
    @Positive private Double maxFare;
    private String specialRequests;
}
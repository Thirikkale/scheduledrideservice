package com.thirikkale.scheduledrideservice.dto;

import lombok.*;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduledRideResponseDto {
    private String id;
    private String riderId;
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String dropoffAddress;
    private Double dropoffLatitude;
    private Double dropoffLongitude;
    private Integer passengers;
    private Boolean isSharedRide;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant scheduledTime;
    
    private String status;
    private String sharedGroupId;
    private String rideType;
    private String vehicleType;
    private Double distanceKm;
    private Integer waitingTimeMin;
    private Boolean isWomenOnly;
    private String driverId;
    private Double maxFare;
    private String specialRequests;
}
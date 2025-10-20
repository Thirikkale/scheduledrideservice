package com.thirikkale.scheduledrideservice.dto;

import lombok.*;

import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RouteMatchResponseDto {
    private String id;
    private String riderId;
    
    // Pickup details
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double pickupDistanceKm;
    
    // Dropoff details
    private String dropoffAddress;
    private Double dropoffLatitude;
    private Double dropoffLongitude;
    private Double dropoffDistanceKm;
    
    // Total matching score (lower is better)
    private Double totalDistanceKm;
    
    // Ride details
    private Instant scheduledTime;
    private String status;
    private Integer passengers;
    private Boolean isSharedRide;
}

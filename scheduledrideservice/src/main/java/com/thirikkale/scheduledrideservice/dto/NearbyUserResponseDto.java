package com.thirikkale.scheduledrideservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NearbyUserResponseDto {
    private String id;
    private String riderId;
    
    // Pickup details
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    
    // Dropoff details
    private String dropoffAddress;
    private Double dropoffLatitude;
    private Double dropoffLongitude;
    
    // Distance from search point
    private Double distanceKm;
    
    // Ride details
    private Instant scheduledTime;
    private String status;
    private Integer passengers;
    private Boolean isSharedRide;
    private String sharedGroupId;
    
    // Ride options
    private String rideType;
    private String vehicleType;
    private Double rideDistanceKm;
    private Integer waitingTimeMin;
    private Boolean isWomenOnly;
    private String driverId;
    private Double maxFare;
    private String specialRequests;
    
    // Estimates
    private BigDecimal estimatedFare;
    private Double estimatedDistanceKm;
    private Integer estimatedDurationMin;
}

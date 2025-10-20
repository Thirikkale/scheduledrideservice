package com.thirikkale.scheduledrideservice.dto;

import lombok.*;

import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NearbyUserResponseDto {
    private String id;
    private String riderId;
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double distanceKm;
    private Instant scheduledTime;
    private String status;
    private Integer passengers;
    private Boolean isSharedRide;
}

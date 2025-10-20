package com.thirikkale.scheduledrideservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RouteMatchRequestDto {
    @NotNull(message = "Pickup latitude is required")
    private Double pickupLatitude;
    
    @NotNull(message = "Pickup longitude is required")
    private Double pickupLongitude;
    
    @NotNull(message = "Dropoff latitude is required")
    private Double dropoffLatitude;
    
    @NotNull(message = "Dropoff longitude is required")
    private Double dropoffLongitude;
    
    @Builder.Default
    private Double pickupRadiusKm = 5.0;
    
    @Builder.Default
    private Double dropoffRadiusKm = 5.0;
}

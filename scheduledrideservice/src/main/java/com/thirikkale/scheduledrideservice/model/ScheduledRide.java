package com.thirikkale.scheduledrideservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import com.thirikkale.scheduledrideservice.model.enums.ScheduledRideStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

@Document(collection = "scheduled_rides")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ScheduledRide {
    @Id
    private String id;

    private String riderId;
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private GeoJsonPoint pickupLocation; // GeoJSON Point for geospatial queries

    private String dropoffAddress;
    private Double dropoffLatitude;
    private Double dropoffLongitude;
    private GeoJsonPoint dropoffLocation; // GeoJSON Point for geospatial queries

    private Integer passengers;
    private Boolean isSharedRide; // true for shared ride request

    @Indexed
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant scheduledTime;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;

    /**
     * Sets pickup coordinates and automatically updates GeoJSON location
     */
    public void setPickupCoordinates(Double latitude, Double longitude) {
        this.pickupLatitude = latitude;
        this.pickupLongitude = longitude;
        this.pickupLocation = GeoJsonPoint.of(latitude, longitude);
    }

    /**
     * Sets dropoff coordinates and automatically updates GeoJSON location
     */
    public void setDropoffCoordinates(Double latitude, Double longitude) {
        this.dropoffLatitude = latitude;
        this.dropoffLongitude = longitude;
        this.dropoffLocation = GeoJsonPoint.of(latitude, longitude);
    }

    /**
     * Ensures GeoJSON points are synced with lat/long values
     * Call this after loading from DB or before saving
     */
    public void syncGeoJsonPoints() {
        if (pickupLatitude != null && pickupLongitude != null && pickupLocation == null) {
            this.pickupLocation = GeoJsonPoint.of(pickupLatitude, pickupLongitude);
        }
        if (dropoffLatitude != null && dropoffLongitude != null && dropoffLocation == null) {
            this.dropoffLocation = GeoJsonPoint.of(dropoffLatitude, dropoffLongitude);
        }
    }
}
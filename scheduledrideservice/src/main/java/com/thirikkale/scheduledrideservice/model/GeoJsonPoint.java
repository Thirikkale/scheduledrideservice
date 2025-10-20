package com.thirikkale.scheduledrideservice.model;

import lombok.*;
import java.util.Arrays;
import java.util.List;

/**
 * GeoJSON Point representation for MongoDB geospatial queries
 * Format: { "type": "Point", "coordinates": [longitude, latitude] }
 * Note: GeoJSON uses [longitude, latitude] order, not [latitude, longitude]
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GeoJsonPoint {
    @Builder.Default
    private String type = "Point";
    private List<Double> coordinates; // [longitude, latitude]

    /**
     * Creates a GeoJSON Point from latitude and longitude
     * @param latitude the latitude
     * @param longitude the longitude
     * @return GeoJSON Point with coordinates in [longitude, latitude] order
     */
    public static GeoJsonPoint of(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return GeoJsonPoint.builder()
                .type("Point")
                .coordinates(Arrays.asList(longitude, latitude)) // GeoJSON uses [lng, lat]
                .build();
    }

    /**
     * Gets the latitude from the coordinates
     * @return latitude (second element of coordinates array)
     */
    public Double getLatitude() {
        return coordinates != null && coordinates.size() > 1 ? coordinates.get(1) : null;
    }

    /**
     * Gets the longitude from the coordinates
     * @return longitude (first element of coordinates array)
     */
    public Double getLongitude() {
        return coordinates != null && !coordinates.isEmpty() ? coordinates.get(0) : null;
    }
}

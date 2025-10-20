# GeoJSON Coordinates Integration

This document explains the GeoJSON coordinate mapping added to the Scheduled Ride Service.

## Overview

The service now internally converts pickup and dropoff latitude/longitude coordinates to GeoJSON Point format for efficient geospatial queries in MongoDB.

## What Changed

### New Model: `GeoJsonPoint`

A new model class representing GeoJSON Point coordinates:
- **Format**: `{ "type": "Point", "coordinates": [longitude, latitude] }`
- **Note**: GeoJSON uses `[longitude, latitude]` order (not `[latitude, longitude]`)

### Updated: `ScheduledRide` Entity

Added two new fields:
- `pickupLocation` - GeoJSON Point for pickup coordinates
- `dropoffLocation` - GeoJSON Point for dropoff coordinates

The entity now has helper methods:
- `setPickupCoordinates(lat, lng)` - Sets both lat/lng and GeoJSON point
- `setDropoffCoordinates(lat, lng)` - Sets both lat/lng and GeoJSON point
- `syncGeoJsonPoints()` - Ensures GeoJSON points are synced with lat/lng

### New: `ScheduledRideMapper`

A mapper utility that automatically handles GeoJSON conversion when:
- Creating entities from DTOs
- Converting entities to response DTOs

### MongoDB Geospatial Indexes

The `MongoIndexConfig` automatically creates 2dsphere indexes on:
- `pickupLocation`
- `dropoffLocation`

These indexes enable efficient geospatial queries.

## Usage Examples

### Automatic Conversion

When you create a scheduled ride, GeoJSON coordinates are automatically generated:

```java
ScheduledRideCreateRequestDto request = new ScheduledRideCreateRequestDto();
request.setPickupLatitude(40.7128);
request.setPickupLongitude(-74.0060);
request.setDropoffLatitude(40.7589);
request.setDropoffLongitude(-73.9851);
// ... other fields

// The service automatically creates GeoJSON points
ScheduledRideResponseDto response = scheduledRideService.scheduleRide(request);
```

### Geospatial Queries (Future Use)

With GeoJSON coordinates and 2dsphere indexes, you can now perform efficient queries:

#### Find rides near a location
```java
// Find scheduled rides within 5km of a point
Point point = new Point(-74.0060, 40.7128); // Note: [lng, lat] order
Distance distance = new Distance(5, Metrics.KILOMETERS);

NearQuery query = NearQuery.near(point).maxDistance(distance);
List<ScheduledRide> nearbyRides = mongoTemplate
    .geoNear(query, ScheduledRide.class, "scheduled_rides")
    .getContent();
```

#### Find rides within a polygon
```java
// Define a polygon (e.g., neighborhood boundaries)
Polygon polygon = new Polygon(
    new Point(-74.0, 40.7),
    new Point(-74.1, 40.7),
    new Point(-74.1, 40.8),
    new Point(-74.0, 40.8),
    new Point(-74.0, 40.7)
);

Query query = Query.query(
    Criteria.where("pickupLocation").within(polygon)
);
List<ScheduledRide> ridesInArea = mongoTemplate.find(query, ScheduledRide.class);
```

#### Find rides within a certain radius
```java
// Find rides with pickup within 2km of a center point
Point center = new Point(-74.0060, 40.7128);
Distance radius = new Distance(2, Metrics.KILOMETERS);
Circle circle = new Circle(center, radius);

Query query = Query.query(
    Criteria.where("pickupLocation").withinSphere(circle)
);
List<ScheduledRide> ridesInRadius = mongoTemplate.find(query, ScheduledRide.class);
```

## Benefits

1. **Performance**: Geospatial indexes dramatically improve location-based query performance
2. **Compatibility**: Standard GeoJSON format works with MongoDB's native geospatial features
3. **Flexibility**: Supports complex queries (near, within, intersects, etc.)
4. **Future-proof**: Ready for advanced location-based features like:
   - Finding nearby drivers
   - Ride matching based on proximity
   - Service area boundaries
   - Heat maps and analytics

## API Compatibility

**Important**: The existing REST API remains unchanged. The GeoJSON conversion is completely internal:
- Clients still send/receive `pickupLatitude`, `pickupLongitude`, etc.
- GeoJSON coordinates are only used internally for database operations
- No changes required to existing API consumers

## Data Migration

For existing rides without GeoJSON coordinates:
- The `syncGeoJsonPoints()` method will automatically populate them on next update
- Or run a one-time migration script:

```java
List<ScheduledRide> rides = scheduledRideRepository.findAll();
rides.forEach(ride -> {
    ride.syncGeoJsonPoints();
});
scheduledRideRepository.saveAll(rides);
```

## Notes

- GeoJSON coordinates are stored in `[longitude, latitude]` order (opposite of common usage)
- Latitude ranges from -90 to 90
- Longitude ranges from -180 to 180
- 2dsphere indexes support queries on a spherical surface (Earth)
- Distances are calculated using spherical geometry for accuracy

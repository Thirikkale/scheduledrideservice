# Timezone Handling Fix Documentation

## Overview
This document outlines the comprehensive changes made to fix timezone handling issues in the Scheduled Ride Service. All timestamps are now consistently stored and transmitted in UTC timezone.

## Changes Summary

### 1. Configuration Files

#### JacksonConfig.java (NEW)
**Location:** `src/main/java/com/thirikkale/scheduledrideservice/config/JacksonConfig.java`

**Purpose:** Global Jackson configuration for UTC timezone handling

**Key Features:**
- Registers JavaTimeModule for Java 8 date/time support
- Disables timestamp serialization (uses ISO-8601 strings instead)
- Sets default timezone to UTC for all Jackson operations
- Sets JVM default timezone to UTC for MongoDB consistency

#### application.yml (UPDATED)
**Location:** `src/main/resources/application.yml`

**New Settings:**
```yaml
spring:
  jackson:
    time-zone: UTC
    serialization:
      write-dates-as-timestamps: false
      write-date-timestamps-as-nanoseconds: false
    deserialization:
      read-date-timestamps-as-nanoseconds: false
    default-property-inclusion: non_null
    date-format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
```

### 2. Entity Classes

All entity classes updated from `LocalDateTime` to `Instant`:

#### ScheduledRide.java
- `scheduledTime`: `LocalDateTime` → `Instant`
- `createdAt`: `LocalDateTime` → `Instant`
- `updatedAt`: `LocalDateTime` → `Instant`
- Added `@JsonFormat` annotations with UTC timezone

#### ScheduledSharedRideGroup.java
- `scheduledWindowStart`: `LocalDateTime` → `Instant`
- `scheduledWindowEnd`: `LocalDateTime` → `Instant`
- `createdAt`: `LocalDateTime` → `Instant`
- `updatedAt`: `LocalDateTime` → `Instant`
- Added `@JsonFormat` annotations with UTC timezone

### 3. DTOs (Data Transfer Objects)

#### ScheduledRideCreateRequestDto.java
- `scheduledTime`: `LocalDateTime` → `Instant`
- Added `@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")`

#### ScheduledRideResponseDto.java
- `scheduledTime`: `LocalDateTime` → `Instant`
- Added `@JsonFormat` annotation for UTC handling

### 4. Repository Layer

#### ScheduledRideRepository.java
All method signatures updated to use `Instant`:
- `findByStatusAndScheduledTimeBetween(ScheduledRideStatus, Instant, Instant)`
- `findByStatusAndScheduledTimeBefore(ScheduledRideStatus, Instant)`
- `findByIsSharedRideTrueAndStatusAndScheduledTimeBetween(ScheduledRideStatus, Instant, Instant)`

### 5. Service Layer

#### ScheduledRideService.java (Interface)
- `dispatchDueSoloRides(Instant dispatchBefore)`

#### ScheduledRideServiceImpl.java
**Key Changes:**
- All `LocalDateTime` replaced with `Instant`
- Added `Instant now = Instant.now()` for timestamp creation
- All save operations now include `createdAt` and `updatedAt` timestamps
- Updated timestamp handling using `Instant` API

#### SharedRideMatchingService.java (Interface)
- `buildOrUpdateGroups(Instant windowStart, Instant windowEnd)`
- `dispatchDueGroups(Instant dispatchBefore)`

#### SharedRideMatchingServiceImpl.java
**Key Changes:**
- All `LocalDateTime` replaced with `Instant`
- Time calculations updated to use `ChronoUnit.MINUTES`
- Example: `instant.minus(10, ChronoUnit.MINUTES)` instead of `localDateTime.minusMinutes(10)`
- Added proper `createdAt` and `updatedAt` timestamp handling

### 6. Scheduler

#### RideScheduler.java
**Updated Time Calculations:**
```java
// Before
LocalDateTime now = LocalDateTime.now();
LocalDateTime windowStart = now.minusMinutes(leadTimeMinutes + 15);

// After
Instant now = Instant.now();
Instant windowStart = now.minus(leadTimeMinutes + 15, ChronoUnit.MINUTES);
```

### 7. Controller

#### ScheduledRideController.java
**Enhanced Debugging Logging:**
```java
@PostMapping
public ResponseEntity<ScheduledRideResponseDto> schedule(@Valid @RequestBody ScheduledRideCreateRequestDto req) {
    Instant receivedTime = req.getScheduledTime();
    
    // Enhanced logging for timezone debugging
    log.info("=== TIMESTAMP DEBUGGING ===");
    log.info("Received scheduledTime from frontend: {}", receivedTime);
    log.info("Timestamp in UTC ISO-8601: {}", UTC_FORMATTER.format(receivedTime));
    log.info("Timestamp epoch millis: {}", receivedTime.toEpochMilli());
    log.info("Timestamp as UTC LocalDateTime: {}", receivedTime.atOffset(ZoneOffset.UTC));
    log.info("Full request: {}", req);
    
    // ... rest of the code
}
```

## Why Instant Instead of LocalDateTime?

### Problems with LocalDateTime:
1. **No Timezone Information:** `LocalDateTime` doesn't carry timezone information
2. **Ambiguous Interpretation:** Can be interpreted differently by different systems
3. **Timezone Conversion Issues:** Prone to errors during serialization/deserialization
4. **MongoDB Storage:** Can lead to inconsistent storage in MongoDB

### Benefits of Instant:
1. **Always UTC:** Represents a point in time in UTC timezone
2. **Unambiguous:** No timezone interpretation needed
3. **MongoDB Compatible:** Stores correctly as UTC timestamps
4. **ISO-8601 Standard:** Serializes to standard `2024-10-19T14:30:00.000Z` format
5. **Flutter Compatible:** Works seamlessly with Flutter's DateTime.parse()

## Expected JSON Format

### Request (from Flutter):
```json
{
  "riderId": "user123",
  "scheduledTime": "2024-10-19T14:30:00.000Z",
  "pickupAddress": "123 Main St",
  ...
}
```

### Response (to Flutter):
```json
{
  "id": "ride456",
  "riderId": "user123",
  "scheduledTime": "2024-10-19T14:30:00.000Z",
  "status": "SCHEDULED",
  ...
}
```

## MongoDB Storage

All timestamps are now stored in MongoDB as:
- Type: `ISODate`
- Format: UTC timestamp
- Example: `ISODate("2024-10-19T14:30:00.000Z")`

## Testing the Fix

### 1. Send a Test Request
```bash
curl -X POST http://localhost:8085/scheduling-service/api/scheduled-rides \
  -H "Content-Type: application/json" \
  -d '{
    "riderId": "test123",
    "pickupAddress": "Test Location",
    "pickupLatitude": 40.7128,
    "pickupLongitude": -74.0060,
    "dropoffAddress": "Test Destination",
    "dropoffLatitude": 40.7580,
    "dropoffLongitude": -73.9855,
    "scheduledTime": "2024-10-19T14:30:00.000Z",
    "passengers": 1,
    "isSharedRide": false,
    "rideType": "STANDARD",
    "vehicleType": "SEDAN",
    "distanceKm": 5.0,
    "waitingTimeMin": 0,
    "maxFare": 20.0
  }'
```

### 2. Check Logs
Look for debug output:
```
=== TIMESTAMP DEBUGGING ===
Received scheduledTime from frontend: 2024-10-19T14:30:00Z
Timestamp in UTC ISO-8601: 2024-10-19T14:30:00Z
Timestamp epoch millis: 1697725800000
Timestamp as UTC LocalDateTime: 2024-10-19T14:30:00Z
```

### 3. Verify MongoDB
Query MongoDB to ensure timestamps are stored correctly:
```javascript
db.scheduled_rides.findOne({}, {scheduledTime: 1, createdAt: 1, updatedAt: 1})
```

Expected output:
```json
{
  "_id": ObjectId("..."),
  "scheduledTime": ISODate("2024-10-19T14:30:00.000Z"),
  "createdAt": ISODate("2024-10-19T12:00:00.000Z"),
  "updatedAt": ISODate("2024-10-19T12:00:00.000Z")
}
```

## Flutter Integration

### Sending Timestamps from Flutter:
```dart
// Convert Flutter DateTime to UTC ISO string
final scheduledTime = DateTime.now().add(Duration(hours: 2));
final body = {
  'scheduledTime': scheduledTime.toUtc().toIso8601String(),
  // ... other fields
};
```

### Parsing Timestamps in Flutter:
```dart
// Parse ISO string to Flutter DateTime
final response = await http.post(...);
final data = jsonDecode(response.body);
final scheduledTime = DateTime.parse(data['scheduledTime']);
// scheduledTime is now in UTC and can be converted to local timezone
final localTime = scheduledTime.toLocal();
```

## Troubleshooting

### Issue: Timestamps are off by several hours
**Solution:** Ensure all services are using UTC. Check JVM timezone setting.

### Issue: MongoDB shows different time than expected
**Solution:** MongoDB stores in UTC. Use `.toISOString()` when querying.

### Issue: Frontend shows wrong time
**Solution:** Make sure Flutter converts UTC to local timezone: `dateTime.toLocal()`

### Issue: Serialization errors
**Solution:** Ensure Jackson dependency includes `jackson-datatype-jsr310` module.

## Dependencies Required

Ensure these are in your `pom.xml`:
```xml
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

## Migration Notes

### For Existing Data:
If you have existing data in MongoDB with `LocalDateTime` values, you may need to run a migration script:

```javascript
// MongoDB migration script
db.scheduled_rides.find().forEach(function(doc) {
  if (doc.scheduledTime && !(doc.scheduledTime instanceof Date)) {
    // Convert string to Date if needed
    db.scheduled_rides.updateOne(
      {_id: doc._id},
      {$set: {
        scheduledTime: new Date(doc.scheduledTime),
        updatedAt: new Date()
      }}
    );
  }
});
```

## Best Practices

1. **Always Use UTC in Backend:** Store and process all timestamps in UTC
2. **Convert to Local in Frontend:** Let the frontend handle timezone display
3. **Use ISO-8601 Format:** Standard format for API communication
4. **Log Timestamps:** Include timezone debugging logs during development
5. **Test Across Timezones:** Test with different timezone scenarios

## Summary

This fix ensures:
- ✅ Consistent UTC timezone handling throughout the application
- ✅ Proper MongoDB storage of timestamps
- ✅ ISO-8601 standard compliance
- ✅ Seamless Flutter integration
- ✅ No timezone conversion issues
- ✅ Enhanced debugging capabilities

All timestamp-related issues should now be resolved with this implementation.

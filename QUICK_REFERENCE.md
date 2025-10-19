# Quick Reference: Timezone Handling

## TL;DR
- All timestamps use `Instant` (not `LocalDateTime`)
- All timestamps stored/transmitted in UTC
- Format: ISO-8601 (`2024-10-19T14:30:00.000Z`)
- MongoDB stores as `ISODate` in UTC
- Frontend handles timezone display conversion

## Code Examples

### Creating a New Timestamp
```java
// ✅ CORRECT
Instant now = Instant.now();
ride.setCreatedAt(now);

// ❌ WRONG
LocalDateTime now = LocalDateTime.now();
```

### Adding Time to Instant
```java
// ✅ CORRECT
Instant future = Instant.now().plus(30, ChronoUnit.MINUTES);

// ❌ WRONG
LocalDateTime future = LocalDateTime.now().plusMinutes(30);
```

### Comparing Instants
```java
// ✅ CORRECT
Duration duration = Duration.between(instant1, instant2);
long minutes = duration.toMinutes();

// ✅ ALSO CORRECT
boolean isBefore = instant1.isBefore(instant2);
boolean isAfter = instant1.isAfter(instant2);
```

### Entity Field Definition
```java
// ✅ CORRECT
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
private Instant scheduledTime;

// ❌ WRONG
private LocalDateTime scheduledTime;
```

### DTO Field Definition
```java
// ✅ CORRECT
@NotNull
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
private Instant scheduledTime;
```

### JSON Request/Response Format
```json
{
  "scheduledTime": "2024-10-19T14:30:00.000Z",
  "createdAt": "2024-10-19T12:00:00.000Z"
}
```

## Flutter Integration

### Sending from Flutter
```dart
final dateTime = DateTime.now().add(Duration(hours: 2));
final isoString = dateTime.toUtc().toIso8601String();

// Use isoString in JSON: "2024-10-19T14:30:00.000Z"
```

### Receiving in Flutter
```dart
final dateTime = DateTime.parse(jsonData['scheduledTime']);
final localTime = dateTime.toLocal(); // Convert to user's timezone
```

## Common Patterns

### Service Layer - Creating Entity
```java
Instant now = Instant.now();
ScheduledRide ride = ScheduledRide.builder()
    .scheduledTime(req.getScheduledTime())
    .createdAt(now)
    .updatedAt(now)
    .build();
```

### Service Layer - Updating Entity
```java
ride.setStatus(ScheduledRideStatus.CANCELLED);
ride.setUpdatedAt(Instant.now());
repo.save(ride);
```

### Scheduler - Time Windows
```java
Instant now = Instant.now();
Instant windowStart = now.minus(15, ChronoUnit.MINUTES);
Instant windowEnd = now.plus(15, ChronoUnit.MINUTES);
```

### Repository Query Methods
```java
List<ScheduledRide> findByStatusAndScheduledTimeBefore(
    ScheduledRideStatus status, 
    Instant before
);

List<ScheduledRide> findByStatusAndScheduledTimeBetween(
    ScheduledRideStatus status, 
    Instant start, 
    Instant end
);
```

## Testing

### Test Request (curl)
```bash
curl -X POST http://localhost:8085/scheduling-service/api/scheduled-rides \
  -H "Content-Type: application/json" \
  -d '{
    "scheduledTime": "2024-10-19T14:30:00.000Z",
    ...
  }'
```

### Expected Log Output
```
=== TIMESTAMP DEBUGGING ===
Received scheduledTime from frontend: 2024-10-19T14:30:00Z
Timestamp in UTC ISO-8601: 2024-10-19T14:30:00Z
Timestamp epoch millis: 1697725800000
```

## MongoDB Queries

### Find documents
```javascript
db.scheduled_rides.find({
  scheduledTime: { 
    $gte: ISODate("2024-10-19T00:00:00.000Z"),
    $lt: ISODate("2024-10-20T00:00:00.000Z")
  }
})
```

### Check timestamp format
```javascript
db.scheduled_rides.findOne({}, {scheduledTime: 1})
// Should return: { scheduledTime: ISODate("2024-10-19T14:30:00.000Z") }
```

## Key Files Modified

1. **Config:** `JacksonConfig.java` - Global UTC configuration
2. **Application:** `application.yml` - Jackson timezone settings
3. **Entities:** `ScheduledRide.java`, `ScheduledSharedRideGroup.java`
4. **DTOs:** `ScheduledRideCreateRequestDto.java`, `ScheduledRideResponseDto.java`
5. **Services:** All service implementations
6. **Repositories:** All repository interfaces
7. **Scheduler:** `RideScheduler.java`
8. **Controller:** `ScheduledRideController.java` (with debug logging)

## Remember

- ✅ Use `Instant` for all timestamps
- ✅ Use `ChronoUnit` for time arithmetic
- ✅ Store in UTC, display in local (frontend responsibility)
- ✅ Always set `updatedAt` when modifying entities
- ✅ Use ISO-8601 format for JSON
- ❌ Never use `LocalDateTime` for API timestamps
- ❌ Never assume timezone in backend
- ❌ Never do timezone conversion in backend

package com.thirikkale.scheduledrideservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.Instant;
import com.thirikkale.scheduledrideservice.model.enums.ScheduledRideStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

@Document(collection = "scheduled_shared_groups")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ScheduledSharedRideGroup {
    @Id
    private String id;

    @Indexed
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant scheduledWindowStart;
    @Indexed
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant scheduledWindowEnd;

    private Double centroidPickupLat;
    private Double centroidPickupLng;

    private Integer maxGroupSize;
    private Integer currentSize;

    private ScheduledRideStatus status; // GROUPING -> SCHEDULED -> DISPATCHED/CANCELLED

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;
}
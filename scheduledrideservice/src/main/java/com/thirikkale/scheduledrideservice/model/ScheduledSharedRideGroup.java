package com.thirikkale.scheduledrideservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDateTime;
import com.thirikkale.scheduledrideservice.model.enums.ScheduledRideStatus;

@Document(collection = "scheduled_shared_groups")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ScheduledSharedRideGroup {
    @Id
    private String id;

    @Indexed
    private LocalDateTime scheduledWindowStart;
    @Indexed
    private LocalDateTime scheduledWindowEnd;

    private Double centroidPickupLat;
    private Double centroidPickupLng;

    private Integer maxGroupSize;
    private Integer currentSize;

    private ScheduledRideStatus status; // GROUPING -> SCHEDULED -> DISPATCHED/CANCELLED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
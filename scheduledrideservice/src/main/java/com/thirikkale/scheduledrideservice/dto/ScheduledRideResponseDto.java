package com.thirikkale.scheduledrideservice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduledRideResponseDto {
    private UUID id;
    private UUID riderId;
    private Boolean shared;
    private Integer passengers;
    private LocalDateTime scheduledTime;
    private String status;
    private UUID sharedGroupId;
}
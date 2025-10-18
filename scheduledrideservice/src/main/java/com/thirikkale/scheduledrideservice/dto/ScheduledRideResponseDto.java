package com.thirikkale.scheduledrideservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduledRideResponseDto {
    private String id;
    private String riderId;
    private Boolean shared;
    private Integer passengers;
    private LocalDateTime scheduledTime;
    private String status;
    private String sharedGroupId;
}
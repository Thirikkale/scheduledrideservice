package com.thirikkale.scheduledrideservice.service;

import com.thirikkale.scheduledrideservice.dto.ScheduledRideCreateRequestDto;
import com.thirikkale.scheduledrideservice.dto.ScheduledRideResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ScheduledRideService {
    ScheduledRideResponseDto scheduleRide(ScheduledRideCreateRequestDto request);
    void cancelRide(UUID id);
    List<UUID> dispatchDueSoloRides(LocalDateTime dispatchBefore);
}
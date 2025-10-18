package com.thirikkale.scheduledrideservice.service;

import com.thirikkale.scheduledrideservice.dto.ScheduledRideCreateRequestDto;
import com.thirikkale.scheduledrideservice.dto.ScheduledRideResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledRideService {
    ScheduledRideResponseDto scheduleRide(ScheduledRideCreateRequestDto request);
    void cancelRide(String id);
    List<String> dispatchDueSoloRides(LocalDateTime dispatchBefore);
}
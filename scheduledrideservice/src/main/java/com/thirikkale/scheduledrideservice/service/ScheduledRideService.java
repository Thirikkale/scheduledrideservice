package com.thirikkale.scheduledrideservice.service;

import com.thirikkale.scheduledrideservice.dto.ScheduledRideCreateRequestDto;
import com.thirikkale.scheduledrideservice.dto.ScheduledRideResponseDto;

import java.time.Instant;
import java.util.List;

public interface ScheduledRideService {
    ScheduledRideResponseDto scheduleRide(ScheduledRideCreateRequestDto request);
    ScheduledRideResponseDto cancelRide(String id);
    List<String> dispatchDueSoloRides(Instant dispatchBefore);
    List<ScheduledRideResponseDto> getAllRides();
    List<ScheduledRideResponseDto> getRidesByRiderId(String riderId);
    List<ScheduledRideResponseDto> getRidesByDriverId(String driverId);
    ScheduledRideResponseDto assignDriverToRide(String rideId, String driverId);
    ScheduledRideResponseDto removeDriverFromRide(String rideId);
}
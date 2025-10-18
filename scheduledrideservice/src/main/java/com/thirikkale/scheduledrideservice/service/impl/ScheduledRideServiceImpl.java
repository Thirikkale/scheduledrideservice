package com.thirikkale.scheduledrideservice.service.impl;

import com.thirikkale.scheduledrideservice.dto.ScheduledRideCreateRequestDto;
import com.thirikkale.scheduledrideservice.dto.ScheduledRideResponseDto;
import com.thirikkale.scheduledrideservice.model.ScheduledRide;
import com.thirikkale.scheduledrideservice.model.enums.ScheduledRideStatus;
import com.thirikkale.scheduledrideservice.repository.ScheduledRideRepository;
import com.thirikkale.scheduledrideservice.service.ScheduledRideService;
import com.thirikkale.scheduledrideservice.messaging.RideRequestPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduledRideServiceImpl implements ScheduledRideService {

    private final ScheduledRideRepository repo;
    private final RideRequestPublisher publisher;

    @Override
    public ScheduledRideResponseDto scheduleRide(ScheduledRideCreateRequestDto req) {
    ScheduledRide ride = ScheduledRide.builder()
        .riderId(req.getRiderId())
        .pickupAddress(req.getPickupAddress())
        .pickupLatitude(req.getPickupLatitude())
        .pickupLongitude(req.getPickupLongitude())
        .dropoffAddress(req.getDropoffAddress())
        .dropoffLatitude(req.getDropoffLatitude())
        .dropoffLongitude(req.getDropoffLongitude())
        .passengers(req.getPassengers())
        .shared(req.getShared())
        .scheduledTime(req.getScheduledTime())
        .status(req.getShared() ? ScheduledRideStatus.GROUPING : ScheduledRideStatus.SCHEDULED)
        .rideType(req.getRideType())
        .vehicleType(req.getVehicleType())
        .distanceKm(req.getDistanceKm())
        .waitingTimeMin(req.getWaitingTimeMin())
        .womenOnly(req.getWomenOnly())
        .maxFare(req.getMaxFare())
        .specialRequests(req.getSpecialRequests())
        .build(); // MongoDB will auto-generate id
        ride = repo.save(ride);
    return ScheduledRideResponseDto.builder()
        .id(ride.getId())
        .riderId(ride.getRiderId())
        .shared(ride.getShared())
        .passengers(ride.getPassengers())
        .scheduledTime(ride.getScheduledTime())
        .status(ride.getStatus().name())
        .sharedGroupId(ride.getSharedGroupId())
        .build();
    }

    @Override
    public void cancelRide(String id) {
        repo.findById(id).ifPresent(r -> {
            r.setStatus(ScheduledRideStatus.CANCELLED);
            repo.save(r);
        });
    }

    @Override
    public List<String> dispatchDueSoloRides(LocalDateTime dispatchBefore) {
        List<ScheduledRide> rides = repo.findByStatusAndScheduledTimeBefore(ScheduledRideStatus.SCHEDULED, dispatchBefore);
        rides.forEach(r -> {
            publisher.publishSoloRideRequest(r);
            r.setStatus(ScheduledRideStatus.DISPATCHED);
        });
        repo.saveAll(rides);
    // Return String ids directly
    return rides.stream().map(ScheduledRide::getId).toList();
    }
}
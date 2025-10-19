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
            .isSharedRide(req.getIsSharedRide())
        .scheduledTime(req.getScheduledTime())
            .status(req.getIsSharedRide() ? ScheduledRideStatus.GROUPING : ScheduledRideStatus.SCHEDULED)
        .rideType(req.getRideType())
        .vehicleType(req.getVehicleType())
        .distanceKm(req.getDistanceKm())
        .waitingTimeMin(req.getWaitingTimeMin())
            .isWomenOnly(req.getIsWomenOnly())
            .driverId(req.getDriverId())
        .maxFare(req.getMaxFare())
        .specialRequests(req.getSpecialRequests())
        .build(); // MongoDB will auto-generate id
    ride = repo.save(ride);
    // Publish solo ride request if not shared
    if (!ride.getIsSharedRide()) {
        publisher.publishSoloRideRequest(ride);
    }
    return ScheduledRideResponseDto.builder()
        .id(ride.getId())
        .riderId(ride.getRiderId())
        .pickupAddress(ride.getPickupAddress())
        .pickupLatitude(ride.getPickupLatitude())
        .pickupLongitude(ride.getPickupLongitude())
        .dropoffAddress(ride.getDropoffAddress())
        .dropoffLatitude(ride.getDropoffLatitude())
        .dropoffLongitude(ride.getDropoffLongitude())
        .passengers(ride.getPassengers())
            .isSharedRide(ride.getIsSharedRide())
        .scheduledTime(ride.getScheduledTime())
        .status(ride.getStatus().name())
        .sharedGroupId(ride.getSharedGroupId())
        .rideType(ride.getRideType())
        .vehicleType(ride.getVehicleType())
        .distanceKm(ride.getDistanceKm())
        .waitingTimeMin(ride.getWaitingTimeMin())
            .isWomenOnly(ride.getIsWomenOnly())
            .driverId(ride.getDriverId())
        .maxFare(ride.getMaxFare())
        .specialRequests(ride.getSpecialRequests())
        .build();
    }

    @Override
    public ScheduledRideResponseDto cancelRide(String id) {
        ScheduledRide ride = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("No ride found with id: " + id));
        ride.setStatus(ScheduledRideStatus.CANCELLED);
        ride = repo.save(ride);
        return ScheduledRideResponseDto.builder()
            .id(ride.getId())
            .riderId(ride.getRiderId())
            .pickupAddress(ride.getPickupAddress())
            .pickupLatitude(ride.getPickupLatitude())
            .pickupLongitude(ride.getPickupLongitude())
            .dropoffAddress(ride.getDropoffAddress())
            .dropoffLatitude(ride.getDropoffLatitude())
            .dropoffLongitude(ride.getDropoffLongitude())
            .passengers(ride.getPassengers())
            .isSharedRide(ride.getIsSharedRide())
            .scheduledTime(ride.getScheduledTime())
            .status(ride.getStatus().name())
            .sharedGroupId(ride.getSharedGroupId())
            .rideType(ride.getRideType())
            .vehicleType(ride.getVehicleType())
            .distanceKm(ride.getDistanceKm())
            .waitingTimeMin(ride.getWaitingTimeMin())
            .isWomenOnly(ride.getIsWomenOnly())
            .driverId(ride.getDriverId())
            .maxFare(ride.getMaxFare())
            .specialRequests(ride.getSpecialRequests())
            .build();
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

    @Override
    public List<ScheduledRideResponseDto> getRidesByRiderId(String riderId) {
        List<ScheduledRide> rides = repo.findByRiderId(riderId);
    return rides.stream().map(r -> ScheduledRideResponseDto.builder()
        .id(r.getId())
        .riderId(r.getRiderId())
        .pickupAddress(r.getPickupAddress())
        .pickupLatitude(r.getPickupLatitude())
        .pickupLongitude(r.getPickupLongitude())
        .dropoffAddress(r.getDropoffAddress())
        .dropoffLatitude(r.getDropoffLatitude())
        .dropoffLongitude(r.getDropoffLongitude())
        .passengers(r.getPassengers())
            .isSharedRide(r.getIsSharedRide())
        .scheduledTime(r.getScheduledTime())
        .status(r.getStatus().name())
        .sharedGroupId(r.getSharedGroupId())
        .rideType(r.getRideType())
        .vehicleType(r.getVehicleType())
        .distanceKm(r.getDistanceKm())
        .waitingTimeMin(r.getWaitingTimeMin())
            .isWomenOnly(r.getIsWomenOnly())
            .driverId(r.getDriverId())
        .maxFare(r.getMaxFare())
        .specialRequests(r.getSpecialRequests())
        .build()).toList();
    }

    @Override
    public ScheduledRideResponseDto assignDriverToRide(String rideId, String driverId) {
        ScheduledRide ride = repo.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Scheduled ride not found with id: " + rideId));
        
        // Only allow driver assignment for scheduled or grouping rides
        if (ride.getStatus() != ScheduledRideStatus.SCHEDULED && ride.getStatus() != ScheduledRideStatus.GROUPING) {
            throw new RuntimeException("Cannot assign driver to ride with status: " + ride.getStatus());
        }
        
        // Check if driver is already assigned
        if (ride.getDriverId() != null && !ride.getDriverId().isEmpty()) {
            throw new RuntimeException("Driver already assigned to this ride. Current driver: " + ride.getDriverId());
        }
        
        ride.setDriverId(driverId);
        ride = repo.save(ride);
        
        return ScheduledRideResponseDto.builder()
                .id(ride.getId())
                .riderId(ride.getRiderId())
                .pickupAddress(ride.getPickupAddress())
                .pickupLatitude(ride.getPickupLatitude())
                .pickupLongitude(ride.getPickupLongitude())
                .dropoffAddress(ride.getDropoffAddress())
                .dropoffLatitude(ride.getDropoffLatitude())
                .dropoffLongitude(ride.getDropoffLongitude())
                .passengers(ride.getPassengers())
                .isSharedRide(ride.getIsSharedRide())
                .scheduledTime(ride.getScheduledTime())
                .status(ride.getStatus().name())
                .sharedGroupId(ride.getSharedGroupId())
                .rideType(ride.getRideType())
                .vehicleType(ride.getVehicleType())
                .distanceKm(ride.getDistanceKm())
                .waitingTimeMin(ride.getWaitingTimeMin())
                .isWomenOnly(ride.getIsWomenOnly())
                .driverId(ride.getDriverId())
                .maxFare(ride.getMaxFare())
                .specialRequests(ride.getSpecialRequests())
                .build();
    }

    @Override
    public ScheduledRideResponseDto removeDriverFromRide(String rideId) {
        ScheduledRide ride = repo.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Scheduled ride not found with id: " + rideId));
        
        // Only allow driver removal for scheduled or grouping rides
        if (ride.getStatus() != ScheduledRideStatus.SCHEDULED && ride.getStatus() != ScheduledRideStatus.GROUPING) {
            throw new RuntimeException("Cannot remove driver from ride with status: " + ride.getStatus());
        }
        
        // Check if driver is assigned
        if (ride.getDriverId() == null || ride.getDriverId().isEmpty()) {
            throw new RuntimeException("No driver assigned to this ride");
        }
        
        ride.setDriverId(null);
        ride = repo.save(ride);
        
        return ScheduledRideResponseDto.builder()
                .id(ride.getId())
                .riderId(ride.getRiderId())
                .pickupAddress(ride.getPickupAddress())
                .pickupLatitude(ride.getPickupLatitude())
                .pickupLongitude(ride.getPickupLongitude())
                .dropoffAddress(ride.getDropoffAddress())
                .dropoffLatitude(ride.getDropoffLatitude())
                .dropoffLongitude(ride.getDropoffLongitude())
                .passengers(ride.getPassengers())
                .isSharedRide(ride.getIsSharedRide())
                .scheduledTime(ride.getScheduledTime())
                .status(ride.getStatus().name())
                .sharedGroupId(ride.getSharedGroupId())
                .rideType(ride.getRideType())
                .vehicleType(ride.getVehicleType())
                .distanceKm(ride.getDistanceKm())
                .waitingTimeMin(ride.getWaitingTimeMin())
                .isWomenOnly(ride.getIsWomenOnly())
                .driverId(ride.getDriverId())
                .maxFare(ride.getMaxFare())
                .specialRequests(ride.getSpecialRequests())
                .build();
    }
}
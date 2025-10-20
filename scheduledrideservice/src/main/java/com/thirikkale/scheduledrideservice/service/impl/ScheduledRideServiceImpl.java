package com.thirikkale.scheduledrideservice.service.impl;

import com.thirikkale.scheduledrideservice.dto.NearbyUserResponseDto;
import com.thirikkale.scheduledrideservice.dto.ScheduledRideCreateRequestDto;
import com.thirikkale.scheduledrideservice.dto.ScheduledRideResponseDto;
import com.thirikkale.scheduledrideservice.mapper.ScheduledRideMapper;
import com.thirikkale.scheduledrideservice.model.ScheduledRide;
import com.thirikkale.scheduledrideservice.model.enums.ScheduledRideStatus;
import com.thirikkale.scheduledrideservice.repository.ScheduledRideRepository;
import com.thirikkale.scheduledrideservice.service.ScheduledRideService;
import com.thirikkale.scheduledrideservice.messaging.RideRequestPublisher;
import com.thirikkale.scheduledrideservice.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduledRideServiceImpl implements ScheduledRideService {
    @Override
    public List<ScheduledRideResponseDto> getRidesByDriverId(String driverId) {
        List<ScheduledRide> rides = repo.findByDriverId(driverId);
        return rides.stream()
                .map(ScheduledRideMapper::toDto)
                .toList();
    }

    private final ScheduledRideRepository repo;
    private final RideRequestPublisher publisher;

    @Override
    public ScheduledRideResponseDto scheduleRide(ScheduledRideCreateRequestDto req) {
        // Use mapper to convert DTO to entity with GeoJSON coordinates
        ScheduledRide ride = ScheduledRideMapper.toEntity(req);
        ride = repo.save(ride);
        
        // Publish solo ride request if not shared
        if (!ride.getIsSharedRide()) {
            publisher.publishSoloRideRequest(ride);
        }
        
        // Use mapper to convert entity back to DTO
        return ScheduledRideMapper.toDto(ride);
    }

    @Override
    public ScheduledRideResponseDto cancelRide(String id) {
        ScheduledRide ride = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("No ride found with id: " + id));
        ride.setStatus(ScheduledRideStatus.CANCELLED);
        ride.setUpdatedAt(Instant.now());
        ride = repo.save(ride);
        return ScheduledRideMapper.toDto(ride);
    }

    @Override
    public List<String> dispatchDueSoloRides(Instant dispatchBefore) {
        List<ScheduledRide> rides = repo.findByStatusAndScheduledTimeBefore(ScheduledRideStatus.SCHEDULED, dispatchBefore);
        rides.forEach(r -> {
            publisher.publishSoloRideRequest(r);
            r.setStatus(ScheduledRideStatus.DISPATCHED);
            r.setUpdatedAt(Instant.now());
        });
        repo.saveAll(rides);
    // Return String ids directly
    return rides.stream().map(ScheduledRide::getId).toList();
    }

    @Override
    public List<ScheduledRideResponseDto> getAllRides() {
        List<ScheduledRide> rides = repo.findAll();
        return rides.stream()
                .map(ScheduledRideMapper::toDto)
                .toList();
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
        ride.setUpdatedAt(Instant.now());
        ride = repo.save(ride);
        
        return ScheduledRideMapper.toDto(ride);
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
        ride.setUpdatedAt(Instant.now());
        ride = repo.save(ride);
        
        return ScheduledRideMapper.toDto(ride);
    }

    @Override
    public List<NearbyUserResponseDto> findNearbyUsers(Double latitude, Double longitude, Double radiusKm) {
        if (latitude == null || longitude == null) {
            throw new RuntimeException("Latitude and longitude are required");
        }
        
        if (radiusKm == null || radiusKm <= 0) {
            throw new RuntimeException("Radius must be a positive number");
        }
        
        // Get all scheduled rides
        List<ScheduledRide> allRides = repo.findAll();
        
        // Filter rides by status (SCHEDULED or GROUPING only), coordinates, and distance
        return allRides.stream()
                .filter(ride -> ride.getStatus() == ScheduledRideStatus.SCHEDULED || 
                               ride.getStatus() == ScheduledRideStatus.GROUPING)
                .filter(ride -> ride.getPickupLatitude() != null && ride.getPickupLongitude() != null)
                .map(ride -> {
                    double distance = DistanceCalculator.calculateDistance(
                            latitude, longitude,
                            ride.getPickupLatitude(), ride.getPickupLongitude()
                    );
                    return new RideDistancePair(ride, distance);
                })
                .filter(pair -> pair.distance <= radiusKm)
                .sorted((a, b) -> Double.compare(a.distance, b.distance))
                .map(pair -> NearbyUserResponseDto.builder()
                        .id(pair.ride.getId())
                        .riderId(pair.ride.getRiderId())
                        .pickupAddress(pair.ride.getPickupAddress())
                        .pickupLatitude(pair.ride.getPickupLatitude())
                        .pickupLongitude(pair.ride.getPickupLongitude())
                        .distanceKm(Math.round(pair.distance * 100.0) / 100.0) // Round to 2 decimal places
                        .scheduledTime(pair.ride.getScheduledTime())
                        .status(pair.ride.getStatus().name())
                        .passengers(pair.ride.getPassengers())
                        .isSharedRide(pair.ride.getIsSharedRide())
                        .build())
                .collect(Collectors.toList());
    }
    
    // Helper class to hold ride and distance pair
    private static class RideDistancePair {
        final ScheduledRide ride;
        final double distance;
        
        RideDistancePair(ScheduledRide ride, double distance) {
            this.ride = ride;
            this.distance = distance;
        }
    }
}
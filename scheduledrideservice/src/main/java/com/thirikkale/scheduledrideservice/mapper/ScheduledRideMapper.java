package com.thirikkale.scheduledrideservice.mapper;

import com.thirikkale.scheduledrideservice.dto.ScheduledRideCreateRequestDto;
import com.thirikkale.scheduledrideservice.dto.ScheduledRideResponseDto;
import com.thirikkale.scheduledrideservice.model.GeoJsonPoint;
import com.thirikkale.scheduledrideservice.model.ScheduledRide;
import com.thirikkale.scheduledrideservice.model.enums.ScheduledRideStatus;

import java.time.Instant;

/**
 * Mapper utility for converting between ScheduledRide entities and DTOs
 * Handles GeoJSON coordinate conversion automatically
 */
public class ScheduledRideMapper {

    /**
     * Maps DTO to entity with GeoJSON coordinates
     */
    public static ScheduledRide toEntity(ScheduledRideCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Instant now = Instant.now();
        ScheduledRide ride = ScheduledRide.builder()
                .riderId(dto.getRiderId())
                .pickupAddress(dto.getPickupAddress())
                .pickupLatitude(dto.getPickupLatitude())
                .pickupLongitude(dto.getPickupLongitude())
                .pickupLocation(GeoJsonPoint.of(dto.getPickupLatitude(), dto.getPickupLongitude()))
                .dropoffAddress(dto.getDropoffAddress())
                .dropoffLatitude(dto.getDropoffLatitude())
                .dropoffLongitude(dto.getDropoffLongitude())
                .dropoffLocation(GeoJsonPoint.of(dto.getDropoffLatitude(), dto.getDropoffLongitude()))
                .passengers(dto.getPassengers())
                .isSharedRide(dto.getIsSharedRide())
                .scheduledTime(dto.getScheduledTime())
                .status(dto.getIsSharedRide() ? ScheduledRideStatus.GROUPING : ScheduledRideStatus.SCHEDULED)
                .rideType(dto.getRideType())
                .vehicleType(dto.getVehicleType())
                .distanceKm(dto.getDistanceKm())
                .waitingTimeMin(dto.getWaitingTimeMin())
                .isWomenOnly(dto.getIsWomenOnly())
                .driverId(dto.getDriverId())
                .maxFare(dto.getMaxFare())
                .specialRequests(dto.getSpecialRequests())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return ride;
    }

    /**
     * Maps entity to response DTO
     */
    public static ScheduledRideResponseDto toDto(ScheduledRide entity) {
        if (entity == null) {
            return null;
        }

        return ScheduledRideResponseDto.builder()
                .id(entity.getId())
                .riderId(entity.getRiderId())
                .pickupAddress(entity.getPickupAddress())
                .pickupLatitude(entity.getPickupLatitude())
                .pickupLongitude(entity.getPickupLongitude())
                .dropoffAddress(entity.getDropoffAddress())
                .dropoffLatitude(entity.getDropoffLatitude())
                .dropoffLongitude(entity.getDropoffLongitude())
                .passengers(entity.getPassengers())
                .isSharedRide(entity.getIsSharedRide())
                .scheduledTime(entity.getScheduledTime())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .sharedGroupId(entity.getSharedGroupId())
                .rideType(entity.getRideType())
                .vehicleType(entity.getVehicleType())
                .distanceKm(entity.getDistanceKm())
                .waitingTimeMin(entity.getWaitingTimeMin())
                .isWomenOnly(entity.getIsWomenOnly())
                .driverId(entity.getDriverId())
                .maxFare(entity.getMaxFare())
                .specialRequests(entity.getSpecialRequests())
                .build();
    }

    private ScheduledRideMapper() {
        // Utility class, prevent instantiation
    }
}

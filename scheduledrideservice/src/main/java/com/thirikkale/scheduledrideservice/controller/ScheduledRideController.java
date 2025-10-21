// ...existing code...

package com.thirikkale.scheduledrideservice.controller;

import com.thirikkale.scheduledrideservice.dto.NearbyUserResponseDto;
import com.thirikkale.scheduledrideservice.dto.RouteMatchRequestDto;
import com.thirikkale.scheduledrideservice.dto.RouteMatchResponseDto;
import com.thirikkale.scheduledrideservice.dto.ScheduledRideCreateRequestDto;
import com.thirikkale.scheduledrideservice.dto.ScheduledRideResponseDto;
import com.thirikkale.scheduledrideservice.dto.ErrorResponseDto;
import com.thirikkale.scheduledrideservice.service.ScheduledRideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


@RestController
@RequestMapping("/api/scheduled-rides")
@RequiredArgsConstructor
public class ScheduledRideController {
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<java.util.List<ScheduledRideResponseDto>> getRidesByDriver(@PathVariable String driverId) {
        log.debug("Fetching rides for driverId: {}", driverId);
        try {
            java.util.List<ScheduledRideResponseDto> rides = scheduledRideService.getRidesByDriverId(driverId);
            log.debug("Found {} rides for driverId: {}", rides.size(), driverId);
            return ResponseEntity.ok(rides);
        } catch (RuntimeException ex) {
            log.error("Error fetching rides: {}", ex.getMessage());
            java.util.List<ScheduledRideResponseDto> errorList = new java.util.ArrayList<>();
            errorList.add(ScheduledRideResponseDto.builder().status("ERROR: " + ex.getMessage()).build());
            return ResponseEntity.status(400).body(errorList);
        }
    }
    private static final Logger log = LoggerFactory.getLogger(ScheduledRideController.class);
    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private final ScheduledRideService scheduledRideService;

    @PostMapping
    public ResponseEntity<ScheduledRideResponseDto> schedule(@Valid @RequestBody ScheduledRideCreateRequestDto req) {
        Instant receivedTime = req.getScheduledTime();
        
        // Enhanced logging for timezone debugging
        log.info("=== TIMESTAMP DEBUGGING ===");
        log.info("Received scheduledTime from frontend: {}", receivedTime);
        log.info("Timestamp in UTC ISO-8601: {}", UTC_FORMATTER.format(receivedTime));
        log.info("Timestamp epoch millis: {}", receivedTime.toEpochMilli());
        log.info("Timestamp as UTC LocalDateTime: {}", receivedTime.atOffset(ZoneOffset.UTC));
        log.info("Full request: {}", req);
        
        try {
            ScheduledRideResponseDto response = scheduledRideService.scheduleRide(req);
            
            log.info("Response scheduledTime: {}", response.getScheduledTime());
            log.info("Response scheduledTime in UTC ISO-8601: {}", 
                response.getScheduledTime() != null ? UTC_FORMATTER.format(response.getScheduledTime()) : "null");
            log.info("=== END TIMESTAMP DEBUGGING ===");
            log.debug("Scheduled ride response: {}", response);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            log.error("Error scheduling ride: {}", ex.getMessage(), ex);
            return ResponseEntity.status(400).body(
                ScheduledRideResponseDto.builder()
                    .status("ERROR: " + ex.getMessage())
                    .build()
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(@PathVariable String id) {
        log.debug("Received cancel request for ride id: {}", id);
        try {
            ScheduledRideResponseDto cancelled = scheduledRideService.cancelRide(id);
            log.debug("Cancelled ride with id: {}", id);
            return ResponseEntity.ok(cancelled);
        } catch (RuntimeException ex) {
            String errorMsg = ex.getMessage();
            log.error("Error cancelling ride: {}", errorMsg);
            return ResponseEntity.status(errorMsg.contains("No ride found") ? 404 : 400).body(
                ErrorResponseDto.builder()
                    .error(errorMsg.contains("No ride found") ? "NOT_FOUND" : "BAD_REQUEST")
                    .message(errorMsg)
                    .build()
            );
        }
    }

    @GetMapping
    public ResponseEntity<java.util.List<ScheduledRideResponseDto>> getAllRides() {
        log.debug("Fetching all scheduled rides");
        try {
            java.util.List<ScheduledRideResponseDto> rides = scheduledRideService.getAllRides();
            log.debug("Found {} total rides", rides.size());
            return ResponseEntity.ok(rides);
        } catch (RuntimeException ex) {
            log.error("Error fetching all rides: {}", ex.getMessage());
            java.util.List<ScheduledRideResponseDto> errorList = new java.util.ArrayList<>();
            errorList.add(ScheduledRideResponseDto.builder().status("ERROR: " + ex.getMessage()).build());
            return ResponseEntity.status(400).body(errorList);
        }
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<java.util.List<ScheduledRideResponseDto>> getRidesByRider(@PathVariable String riderId) {
        log.debug("Fetching rides for riderId: {}", riderId);
        try {
            java.util.List<ScheduledRideResponseDto> rides = scheduledRideService.getRidesByRiderId(riderId);
            log.debug("Found {} rides for riderId: {}", rides.size(), riderId);
            return ResponseEntity.ok(rides);
        } catch (RuntimeException ex) {
            log.error("Error fetching rides: {}", ex.getMessage());
            java.util.List<ScheduledRideResponseDto> errorList = new java.util.ArrayList<>();
            errorList.add(ScheduledRideResponseDto.builder().status("ERROR: " + ex.getMessage()).build());
            return ResponseEntity.status(400).body(errorList);
        }
    }

    @PutMapping("/{rideId}/assign-driver/{driverId}")
    public ResponseEntity<ScheduledRideResponseDto> assignDriver(
            @PathVariable String rideId,
            @PathVariable String driverId) {
        log.debug("Assigning driver {} to ride {}", driverId, rideId);
        try {
            ScheduledRideResponseDto response = scheduledRideService.assignDriverToRide(rideId, driverId);
            log.debug("Driver assigned successfully to ride {}", rideId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            log.error("Error assigning driver: {}", ex.getMessage());
            return ResponseEntity.status(400).body(
                ScheduledRideResponseDto.builder()
                    .status("ERROR: " + ex.getMessage())
                    .build()
            );
        }
    }

    @DeleteMapping("/{rideId}/remove-driver")
    public ResponseEntity<?> removeDriver(@PathVariable String rideId) {
        log.debug("Removing driver from ride {}", rideId);
        try {
            ScheduledRideResponseDto response = scheduledRideService.removeDriverFromRide(rideId);
            log.debug("Driver removed successfully from ride {}", rideId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            log.error("Error removing driver: {}", ex.getMessage());
            return ResponseEntity.status(400).body(
                ErrorResponseDto.builder()
                    .error("BAD_REQUEST")
                    .message(ex.getMessage())
                    .build()
            );
        }
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyUsers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {
        log.debug("Finding nearby users (SCHEDULED/GROUPING only) - lat: {}, lon: {}, radius: {} km", latitude, longitude, radiusKm);
        try {
            java.util.List<NearbyUserResponseDto> nearbyUsers = scheduledRideService.findNearbyUsers(latitude, longitude, radiusKm);
            log.debug("Found {} nearby users within {} km", nearbyUsers.size(), radiusKm);
            return ResponseEntity.ok(nearbyUsers);
        } catch (RuntimeException ex) {
            log.error("Error finding nearby users: {}", ex.getMessage());
            return ResponseEntity.status(400).body(
                ErrorResponseDto.builder()
                    .error("BAD_REQUEST")
                    .message(ex.getMessage())
                    .build()
            );
        }
    }

    @GetMapping("/nearby-dropoff")
    public ResponseEntity<?> getNearbyUsersByDropoff(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {
        log.debug("Finding users with nearby dropoff points (SCHEDULED/GROUPING only) - lat: {}, lon: {}, radius: {} km", 
                  latitude, longitude, radiusKm);
        try {
            java.util.List<NearbyUserResponseDto> nearbyUsers = scheduledRideService.findNearbyUsersByDropoff(latitude, longitude, radiusKm);
            log.debug("Found {} users with dropoff points within {} km", nearbyUsers.size(), radiusKm);
            return ResponseEntity.ok(nearbyUsers);
        } catch (RuntimeException ex) {
            log.error("Error finding nearby dropoff users: {}", ex.getMessage());
            return ResponseEntity.status(400).body(
                ErrorResponseDto.builder()
                    .error("BAD_REQUEST")
                    .message(ex.getMessage())
                    .build()
            );
        }
    }

    @PostMapping("/route-match")
    public ResponseEntity<?> findRouteMatches(@Valid @RequestBody RouteMatchRequestDto request) {
        log.debug("Finding route matches - pickup: ({}, {}), dropoff: ({}, {}), pickup radius: {} km, dropoff radius: {} km",
                  request.getPickupLatitude(), request.getPickupLongitude(),
                  request.getDropoffLatitude(), request.getDropoffLongitude(),
                  request.getPickupRadiusKm(), request.getDropoffRadiusKm());
        try {
            java.util.List<RouteMatchResponseDto> matches = scheduledRideService.findRouteMatches(request);
            log.debug("Found {} route matches", matches.size());
            return ResponseEntity.ok(matches);
        } catch (RuntimeException ex) {
            log.error("Error finding route matches: {}", ex.getMessage());
            return ResponseEntity.status(400).body(
                ErrorResponseDto.builder()
                    .error("BAD_REQUEST")
                    .message(ex.getMessage())
                    .build()
            );
        }
    }

    @PutMapping("/{rideId}/status")
    public ResponseEntity<?> changeStatus(@PathVariable String rideId, 
                                          @Valid @RequestBody com.thirikkale.scheduledrideservice.dto.ChangeStatusRequestDto request) {
        log.debug("Changing status for ride {} to {}", rideId, request.getStatus());
        try {
            // Parse the status string to enum
            com.thirikkale.scheduledrideservice.model.enums.ScheduledRideStatus newStatus = 
                com.thirikkale.scheduledrideservice.model.enums.ScheduledRideStatus.valueOf(request.getStatus().toUpperCase());
            
            ScheduledRideResponseDto response = scheduledRideService.changeRideStatus(rideId, newStatus);
            log.debug("Successfully changed status for ride {}", rideId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid status: {}", request.getStatus());
            return ResponseEntity.status(400).body(
                ErrorResponseDto.builder()
                    .error("INVALID_STATUS")
                    .message("Invalid status. Valid values are: SCHEDULED, GROUPING, DISPATCHED, CANCELLED, ONGOING, ENDED")
                    .build()
            );
        } catch (RuntimeException ex) {
            log.error("Error changing ride status: {}", ex.getMessage());
            return ResponseEntity.status(ex.getMessage().contains("No ride found") ? 404 : 400).body(
                ErrorResponseDto.builder()
                    .error(ex.getMessage().contains("No ride found") ? "NOT_FOUND" : "BAD_REQUEST")
                    .message(ex.getMessage())
                    .build()
            );
        }
    }
}
// ...existing code...

package com.thirikkale.scheduledrideservice.controller;

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
    private final ScheduledRideService scheduledRideService;

    @PostMapping
    public ResponseEntity<ScheduledRideResponseDto> schedule(@Valid @RequestBody ScheduledRideCreateRequestDto req) {
        log.debug("Received schedule request: {}", req);
        try {
            ScheduledRideResponseDto response = scheduledRideService.scheduleRide(req);
            log.debug("Scheduled ride response: {}", response);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            log.error("Error scheduling ride: {}", ex.getMessage());
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
}
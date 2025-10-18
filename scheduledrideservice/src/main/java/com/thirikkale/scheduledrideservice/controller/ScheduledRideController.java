// ...existing code...

package com.thirikkale.scheduledrideservice.controller;

import com.thirikkale.scheduledrideservice.dto.ScheduledRideCreateRequestDto;
import com.thirikkale.scheduledrideservice.dto.ScheduledRideResponseDto;
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
    private static final Logger log = LoggerFactory.getLogger(ScheduledRideController.class);
    private final ScheduledRideService scheduledRideService;

    @PostMapping
    public ResponseEntity<ScheduledRideResponseDto> schedule(@Valid @RequestBody ScheduledRideCreateRequestDto req) {
        log.debug("Received schedule request: {}", req);
        ScheduledRideResponseDto response = scheduledRideService.scheduleRide(req);
        log.debug("Scheduled ride response: {}", response);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable String id) {
        log.debug("Received cancel request for ride id: {}", id);
        boolean deleted = scheduledRideService.cancelRide(id);
        if (deleted) {
            log.debug("Cancelled ride with id: {}", id);
            return ResponseEntity.noContent().build();
        } else {
            log.debug("No ride found with id: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<java.util.List<ScheduledRideResponseDto>> getRidesByRider(@PathVariable String riderId) {
        log.debug("Fetching rides for riderId: {}", riderId);
        java.util.List<ScheduledRideResponseDto> rides = scheduledRideService.getRidesByRiderId(riderId);
        log.debug("Found {} rides for riderId: {}", rides.size(), riderId);
        return ResponseEntity.ok(rides);
    }
}
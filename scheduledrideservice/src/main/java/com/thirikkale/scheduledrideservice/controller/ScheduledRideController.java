package com.thirikkale.scheduledrideservice.controller;

import com.thirikkale.scheduledrideservice.dto.ScheduledRideCreateRequestDto;
import com.thirikkale.scheduledrideservice.dto.ScheduledRideResponseDto;
import com.thirikkale.scheduledrideservice.service.ScheduledRideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/scheduled-rides")
@RequiredArgsConstructor
public class ScheduledRideController {
    private final ScheduledRideService scheduledRideService;

    @PostMapping
    public ResponseEntity<ScheduledRideResponseDto> schedule(@Valid @RequestBody ScheduledRideCreateRequestDto req) {
        return ResponseEntity.ok(scheduledRideService.scheduleRide(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable String id) {
        scheduledRideService.cancelRide(id);
        return ResponseEntity.noContent().build();
    }
}
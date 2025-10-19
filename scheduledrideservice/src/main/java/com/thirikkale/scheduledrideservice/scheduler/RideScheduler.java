package com.thirikkale.scheduledrideservice.scheduler;

import com.thirikkale.scheduledrideservice.service.SharedRideMatchingService;
import com.thirikkale.scheduledrideservice.service.ScheduledRideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideScheduler {

    private final ScheduledRideService scheduledRideService;
    private final SharedRideMatchingService sharedRideMatchingService;

    @Value("${scheduler.dispatch.leadTimeMinutes:10}") private int leadTimeMinutes;

    @Scheduled(fixedDelayString = "${scheduler.matching.interval:120000}")
    public void matchSharedRides() {
        Instant now = Instant.now();
        Instant windowStart = now.minus(leadTimeMinutes + 15, ChronoUnit.MINUTES);
        Instant windowEnd = now.plus(leadTimeMinutes + 15, ChronoUnit.MINUTES);
        sharedRideMatchingService.buildOrUpdateGroups(windowStart, windowEnd);
    }

    @Scheduled(fixedDelayString = "${scheduler.dispatch.interval:30000}")
    public void dispatchDue() {
        Instant dispatchBefore = Instant.now().plus(leadTimeMinutes, ChronoUnit.MINUTES);
        scheduledRideService.dispatchDueSoloRides(dispatchBefore);
        sharedRideMatchingService.dispatchDueGroups(dispatchBefore);
    }
}
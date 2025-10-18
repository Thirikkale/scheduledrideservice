package com.thirikkale.scheduledrideservice.scheduler;

import com.thirikkale.scheduledrideservice.service.SharedRideMatchingService;
import com.thirikkale.scheduledrideservice.service.ScheduledRideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideScheduler {

    private final ScheduledRideService scheduledRideService;
    private final SharedRideMatchingService sharedRideMatchingService;

    @Value("${scheduler.dispatch.leadTimeMinutes:10}") private int leadTimeMinutes;

    @Scheduled(fixedDelayString = "${scheduler.matching.interval:120000}")
    public void matchSharedRides() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(leadTimeMinutes + 15);
        LocalDateTime windowEnd = now.plusMinutes(leadTimeMinutes + 15);
        sharedRideMatchingService.buildOrUpdateGroups(windowStart, windowEnd);
    }

    @Scheduled(fixedDelayString = "${scheduler.dispatch.interval:30000}")
    public void dispatchDue() {
        LocalDateTime dispatchBefore = LocalDateTime.now().plusMinutes(leadTimeMinutes);
        scheduledRideService.dispatchDueSoloRides(dispatchBefore);
        sharedRideMatchingService.dispatchDueGroups(dispatchBefore);
    }
}
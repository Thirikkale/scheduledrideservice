package com.thirikkale.scheduledrideservice.service;

import java.time.LocalDateTime;

public interface SharedRideMatchingService {
    void buildOrUpdateGroups(LocalDateTime windowStart, LocalDateTime windowEnd);
    void dispatchDueGroups(LocalDateTime dispatchBefore);
}
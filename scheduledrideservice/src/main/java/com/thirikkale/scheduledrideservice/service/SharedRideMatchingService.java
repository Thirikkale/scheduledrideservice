package com.thirikkale.scheduledrideservice.service;

import java.time.Instant;

public interface SharedRideMatchingService {
    void buildOrUpdateGroups(Instant windowStart, Instant windowEnd);
    void dispatchDueGroups(Instant dispatchBefore);
}
package com.thirikkale.scheduledrideservice.repository;

import com.thirikkale.scheduledrideservice.model.ScheduledSharedRideGroup;
import com.thirikkale.scheduledrideservice.model.enums.ScheduledRideStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface ScheduledSharedRideGroupRepository extends MongoRepository<ScheduledSharedRideGroup, String> {
    List<ScheduledSharedRideGroup> findByStatusAndScheduledWindowStartLessThanEqualAndScheduledWindowEndGreaterThanEqual(
            ScheduledRideStatus status, Instant end, Instant start);
}
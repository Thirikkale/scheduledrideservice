package com.thirikkale.scheduledrideservice.repository;

import com.thirikkale.scheduledrideservice.model.ScheduledRide;
import com.thirikkale.scheduledrideservice.model.enums.ScheduledRideStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ScheduledRideRepository extends MongoRepository<ScheduledRide, UUID> {
    List<ScheduledRide> findByStatusAndScheduledTimeBetween(
            ScheduledRideStatus status, LocalDateTime start, LocalDateTime end);

    List<ScheduledRide> findByStatusAndScheduledTimeBefore(
            ScheduledRideStatus status, LocalDateTime before);

    List<ScheduledRide> findBySharedTrueAndStatusAndScheduledTimeBetween(
            ScheduledRideStatus status, LocalDateTime start, LocalDateTime end);
}
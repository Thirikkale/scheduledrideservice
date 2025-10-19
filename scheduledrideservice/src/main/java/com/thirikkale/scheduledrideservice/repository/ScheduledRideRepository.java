package com.thirikkale.scheduledrideservice.repository;

import com.thirikkale.scheduledrideservice.model.ScheduledRide;
import com.thirikkale.scheduledrideservice.model.enums.ScheduledRideStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface ScheduledRideRepository extends MongoRepository<ScheduledRide, String> {
    List<ScheduledRide> findByStatusAndScheduledTimeBetween(
                                ScheduledRideStatus status, Instant start, Instant end);

    List<ScheduledRide> findByStatusAndScheduledTimeBefore(
                                ScheduledRideStatus status, Instant before);

    List<ScheduledRide> findByIsSharedRideTrueAndStatusAndScheduledTimeBetween(
                                ScheduledRideStatus status, Instant start, Instant end);

        List<ScheduledRide> findByRiderId(String riderId);
        List<ScheduledRide> findByDriverId(String driverId);
}
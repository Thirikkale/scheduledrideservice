package com.thirikkale.scheduledrideservice.service.impl;

import com.thirikkale.scheduledrideservice.model.ScheduledRide;
import com.thirikkale.scheduledrideservice.model.ScheduledSharedRideGroup;
import com.thirikkale.scheduledrideservice.model.ScheduledSharedRideMember;
import com.thirikkale.scheduledrideservice.model.enums.ScheduledRideStatus;
import com.thirikkale.scheduledrideservice.repository.*;
import com.thirikkale.scheduledrideservice.service.SharedRideMatchingService;
import com.thirikkale.scheduledrideservice.messaging.RideRequestPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import static java.lang.Math.*;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedRideMatchingServiceImpl implements SharedRideMatchingService {

    private final ScheduledRideRepository rideRepo;
    private final ScheduledSharedRideGroupRepository groupRepo;
    private final ScheduledSharedRideMemberRepository memberRepo;
    private final RideRequestPublisher publisher;

    @Value("${scheduler.matching.timeWindowMinutes:10}") private int timeWindowMinutes;
    @Value("${scheduler.matching.pickupRadiusMeters:800}") private int pickupRadiusMeters;
    @Value("${scheduler.matching.maxGroupSize:3}") private int maxGroupSize;
    @Value("${scheduler.matching.maxDetourMinutes:8}") private int maxDetourMinutes;

    @Override
    public void buildOrUpdateGroups(LocalDateTime windowStart, LocalDateTime windowEnd) {
    java.util.List<ScheduledRide> candidates = rideRepo.findBySharedTrueAndStatusAndScheduledTimeBetween(
                ScheduledRideStatus.GROUPING, windowStart, windowEnd);

        // Simple greedy clustering by time window and pickup proximity
        candidates.sort(java.util.Comparator.comparing(ScheduledRide::getScheduledTime));
        for (ScheduledRide r : candidates) {
            if (r.getSharedGroupId() != null) continue;

            // find nearby, close-time rides
            java.util.List<ScheduledRide> cluster = new java.util.ArrayList<>();
            cluster.add(r);
            for (ScheduledRide other : candidates) {
                if (other == r || other.getSharedGroupId() != null) continue;
                if (abs(minutesBetween(r.getScheduledTime(), other.getScheduledTime())) <= timeWindowMinutes
                        && distanceMeters(r.getPickupLatitude(), r.getPickupLongitude(),
                                          other.getPickupLatitude(), other.getPickupLongitude()) <= pickupRadiusMeters) {
                    cluster.add(other);
                }
                if (cluster.size() >= maxGroupSize) break;
            }

            if (cluster.size() >= 2) {
                double[] centroid = centroid(cluster);
                ScheduledSharedRideGroup group = ScheduledSharedRideGroup.builder()
                        .scheduledWindowStart(r.getScheduledTime().minusMinutes(timeWindowMinutes))
                        .scheduledWindowEnd(r.getScheduledTime().plusMinutes(timeWindowMinutes))
                        .centroidPickupLat(centroid[0])
                        .centroidPickupLng(centroid[1])
                        .maxGroupSize(maxGroupSize)
                        .currentSize(cluster.size())
                        .status(ScheduledRideStatus.SCHEDULED)
                        .build();
                group = groupRepo.save(group);

                for (ScheduledRide m : cluster) {
                    m.setSharedGroupId(group.getId()); // group id is now String
                    m.setStatus(ScheduledRideStatus.SCHEDULED);
                    rideRepo.save(m);
                    memberRepo.save(ScheduledSharedRideMember.builder()
                            .groupId(group.getId()).rideId(m.getId()).build()); // ids are String
                }
            }
        }
    }

    @Override
    public void dispatchDueGroups(LocalDateTime dispatchBefore) {
    // Find rides in groups due for dispatch
    // Use a reasonable lower bound instead of LocalDateTime.MIN to avoid conversion errors
    LocalDateTime lowerBound = LocalDateTime.now().minusYears(1); // or another safe default
    java.util.List<ScheduledRide> due = rideRepo.findBySharedTrueAndStatusAndScheduledTimeBetween(
        ScheduledRideStatus.SCHEDULED,
        lowerBound, dispatchBefore);

        // Group by sharedGroupId and dispatch each group once
    due.stream()
           .filter(r -> r.getSharedGroupId() != null)
           .map(ScheduledRide::getSharedGroupId)
           .distinct()
           .forEach(groupId -> {
           java.util.List<ScheduledRide> members = memberRepo.findByGroupId(groupId).stream()
                       .map(m -> rideRepo.findById(m.getRideId()).orElse(null))
                       .filter(Objects::nonNull)
                       .toList();
               if (!members.isEmpty()) {
                   publisher.publishSharedRideGroupRequest(groupId, members);
                   members.forEach(m -> m.setStatus(ScheduledRideStatus.DISPATCHED));
                   rideRepo.saveAll(members);
                   groupRepo.findById(groupId).ifPresent(g -> { g.setStatus(ScheduledRideStatus.DISPATCHED); groupRepo.save(g); });
               }
           });
    }

    private static long minutesBetween(LocalDateTime a, LocalDateTime b) {
        return Duration.between(a, b).abs().toMinutes();
    }

    private static double[] centroid(List<ScheduledRide> rides) {
        double lat = 0, lng = 0;
        for (ScheduledRide r : rides) { // <-- FIXED: replaced 'var' with 'ScheduledRide'
            lat += r.getPickupLatitude();
            lng += r.getPickupLongitude();
        }
        return new double[]{lat / rides.size(), lng / rides.size()};
    }

    private static double distanceMeters(Double lat1, Double lon1, Double lat2, Double lon2) {
        double R = 6371000.0;
        double dLat = toRadians(lat2 - lat1);
        double dLon = toRadians(lon2 - lon1);
        double a = sin(dLat/2)*sin(dLat/2) + cos(toRadians(lat1))*cos(toRadians(lat2))*sin(dLon/2)*sin(dLon/2);
        double c = 2 * atan2(sqrt(a), sqrt(1-a));
        return R * c;
    }
}
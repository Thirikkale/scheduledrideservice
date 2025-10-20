package com.thirikkale.scheduledrideservice.messaging;

import com.thirikkale.scheduledrideservice.model.ScheduledRide;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideRequestPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange rideRequestsExchange;

    @Value("${rabbitmq.routing.solo}") private String soloRoutingKey;
    @Value("${rabbitmq.routing.shared}") private String sharedRoutingKey;

    public void publishSoloRideRequest(ScheduledRide r) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("rideId", r.getId());
            payload.put("riderId", r.getRiderId());
            payload.put("pickupLat", r.getPickupLatitude());
            payload.put("pickupLng", r.getPickupLongitude());
            payload.put("dropoffLat", r.getDropoffLatitude());
            payload.put("dropoffLng", r.getDropoffLongitude());
            payload.put("scheduledTime", r.getScheduledTime().toString());
            payload.put("passengers", r.getPassengers());
            payload.put("isShared", false);
            payload.put("rideType", r.getRideType());
            payload.put("vehicleType", r.getVehicleType());
            payload.put("distanceKm", r.getDistanceKm());
            payload.put("waitingTimeMin", r.getWaitingTimeMin());
            payload.put("womenOnly", r.getIsWomenOnly());
            payload.put("driverId", r.getDriverId());
            payload.put("maxFare", r.getMaxFare());
            payload.put("specialRequests", r.getSpecialRequests());
            rabbitTemplate.convertAndSend(rideRequestsExchange.getName(), soloRoutingKey, payload);
            log.info("Published solo ride request for ride ID: {}", r.getId());
        } catch (Exception e) {
            log.warn("Failed to publish solo ride request to RabbitMQ (ride will still be saved): {}", e.getMessage());
        }
    }

    public void publishSharedRideGroupRequest(String groupId, java.util.List<ScheduledRide> members) {
        try {
            java.util.List<Map<String, Object>> riders = new ArrayList<>();
            for (ScheduledRide r : members) {
                riders.add(Map.of(
                        "scheduledRideId", r.getId(),
                        "riderId", r.getRiderId().toString(),
                        "pickupLat", r.getPickupLatitude(),
                        "pickupLng", r.getPickupLongitude(),
                        "dropoffLat", r.getDropoffLatitude(),
                        "dropoffLng", r.getDropoffLongitude(),
                        "passengers", r.getPassengers()
                ));
            }
            Map<String, Object> payload = Map.of(
                    "groupId", groupId.toString(),
                    "scheduledTime", members.get(0).getScheduledTime().toString(),
                    "members", riders,
                    "isShared", true
            );
            rabbitTemplate.convertAndSend(rideRequestsExchange.getName(), sharedRoutingKey, payload);
            log.info("Published shared ride group request for group ID: {}", groupId);
        } catch (Exception e) {
            log.warn("Failed to publish shared ride group request to RabbitMQ (rides will still be saved): {}", e.getMessage());
        }
    }
}
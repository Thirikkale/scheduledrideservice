package com.thirikkale.scheduledrideservice.messaging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thirikkale.scheduledrideservice.model.ScheduledRide;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class RideRequestPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange rideRequestsExchange;

    @Value("${rabbitmq.routing.solo}") private String soloRoutingKey;
    @Value("${rabbitmq.routing.shared}") private String sharedRoutingKey;

    public void publishSoloRideRequest(ScheduledRide r) {
        Map<String, Object> payload = Map.of(
                "rideId", r.getId().toString(),
                "riderId", r.getRiderId().toString(),
                "pickupLat", r.getPickupLatitude(),
                "pickupLng", r.getPickupLongitude(),
                "dropoffLat", r.getDropoffLatitude(),
                "dropoffLng", r.getDropoffLongitude(),
                "scheduledTime", r.getScheduledTime().toString(),
                "passengers", r.getPassengers(),
                "isShared", false
        );
        rabbitTemplate.convertAndSend(rideRequestsExchange.getName(), soloRoutingKey, payload);
    }

    public void publishSharedRideGroupRequest(java.util.UUID groupId, java.util.List<ScheduledRide> members) {
    java.util.List<Map<String, Object>> riders = new ArrayList<>();
    for (ScheduledRide r : members) {
            riders.add(Map.of(
                    "scheduledRideId", r.getId().toString(),
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
    }
}
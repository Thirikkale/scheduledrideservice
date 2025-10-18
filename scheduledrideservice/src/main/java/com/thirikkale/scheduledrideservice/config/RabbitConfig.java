package com.thirikkale.scheduledrideservice.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${rabbitmq.exchange:ride.requests.exchange}")
    private String exchangeName;

    @Bean
    public TopicExchange rideRequestsExchange() {
        return new TopicExchange(exchangeName);
    }
}

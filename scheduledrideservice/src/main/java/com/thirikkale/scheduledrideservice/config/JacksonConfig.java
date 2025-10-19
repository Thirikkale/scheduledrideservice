package com.thirikkale.scheduledrideservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.TimeZone;

/**
 * Jackson configuration to handle all date/time serialization in UTC timezone.
 * This ensures consistent timestamp handling between MongoDB, Spring Boot, and Flutter frontend.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        
        // Register JavaTimeModule for Java 8 date/time types support
        objectMapper.registerModule(new JavaTimeModule());
        
        // Disable writing dates as timestamps (use ISO-8601 string format instead)
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Set default timezone to UTC for all date/time operations
        objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        return objectMapper;
    }
    
    /**
     * Set the JVM default timezone to UTC.
     * This ensures MongoDB and all date operations use UTC consistently.
     */
    @Bean
    public TimeZone defaultTimeZone() {
        TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
        TimeZone.setDefault(utcTimeZone);
        return utcTimeZone;
    }
}

package com.thirikkale.scheduledrideservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import lombok.extern.slf4j.Slf4j;

/**
 * MongoDB configuration
 * Using manual distance calculations instead of geospatial queries
 */
@Configuration
@Slf4j
public class MongoIndexConfig {

    @EventListener(ApplicationReadyEvent.class)
    public void initIndexes() {
        try {
            // Indexes are no longer needed for geospatial queries
            // Using manual distance calculations instead
            // The @Indexed annotation on scheduledTime field handles that index automatically
            
            log.info("MongoDB configuration initialized - using manual distance calculations");
        } catch (Exception e) {
            log.error("Failed to initialize MongoDB configuration", e);
        }
    }
}

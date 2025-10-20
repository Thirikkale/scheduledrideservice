package com.thirikkale.scheduledrideservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MongoDB configuration for creating geospatial indexes
 * This enables efficient location-based queries using GeoJSON coordinates
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class MongoIndexConfig {

    private final MongoTemplate mongoTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initIndexes() {
        try {
            // Create 2dsphere indexes for geospatial queries on GeoJSON points
            // The GeospatialIndex constructor with field name creates a 2dsphere index by default
            var indexOps = mongoTemplate.indexOps("scheduled_rides");
            
            indexOps.ensureIndex(new GeospatialIndex("pickupLocation"));
            indexOps.ensureIndex(new GeospatialIndex("dropoffLocation"));
            
            log.info("MongoDB geospatial indexes created successfully for scheduled_rides collection");
        } catch (Exception e) {
            log.error("Failed to create MongoDB geospatial indexes", e);
        }
    }
}

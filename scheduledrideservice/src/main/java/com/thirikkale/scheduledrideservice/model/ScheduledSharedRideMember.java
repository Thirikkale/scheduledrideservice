package com.thirikkale.scheduledrideservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document(collection = "scheduled_shared_group_members")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ScheduledSharedRideMember {
    @Id
    private String id;

    @Indexed
    private String groupId;
    @Indexed
    private String rideId;
}
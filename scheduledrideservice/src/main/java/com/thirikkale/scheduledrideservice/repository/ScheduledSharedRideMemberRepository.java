package com.thirikkale.scheduledrideservice.repository;

import com.thirikkale.scheduledrideservice.model.ScheduledSharedRideMember;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface ScheduledSharedRideMemberRepository extends MongoRepository<ScheduledSharedRideMember, UUID> {
    List<ScheduledSharedRideMember> findByGroupId(UUID groupId);
}
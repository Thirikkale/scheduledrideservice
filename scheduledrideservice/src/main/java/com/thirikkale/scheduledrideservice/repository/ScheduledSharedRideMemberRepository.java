package com.thirikkale.scheduledrideservice.repository;

import com.thirikkale.scheduledrideservice.model.ScheduledSharedRideMember;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ScheduledSharedRideMemberRepository extends MongoRepository<ScheduledSharedRideMember, String> {
    List<ScheduledSharedRideMember> findByGroupId(String groupId);
}
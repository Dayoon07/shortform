package com.e.shortform.model.repository;

import com.e.shortform.model.entity.FollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepo extends JpaRepository<FollowEntity, Long> {
}

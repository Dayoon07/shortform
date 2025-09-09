package com.e.shortform.model.repository;

import com.e.shortform.model.entity.CommunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityRepo extends JpaRepository<CommunityEntity, Long> {
}

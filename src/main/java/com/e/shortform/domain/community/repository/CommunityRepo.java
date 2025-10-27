package com.e.shortform.model.repository;

import com.e.shortform.model.entity.CommunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityRepo extends JpaRepository<CommunityEntity, Long> {
    boolean existsByCommunityUuid(String communityUuid);
    CommunityEntity findByCommunityUuid(String communityUuid);
}

package com.e.shortform.domain.community.repository;

import com.e.shortform.domain.community.entity.CommunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityRepo extends JpaRepository<CommunityEntity, Long> {
    boolean existsByCommunityUuid(String communityUuid);
    CommunityEntity findByCommunityUuid(String communityUuid);
}

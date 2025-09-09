package com.e.shortform.model.repository;

import com.e.shortform.model.entity.CommunityLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityLikeRepo extends JpaRepository<CommunityLikeEntity, Long> {
}

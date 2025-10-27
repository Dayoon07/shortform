package com.e.shortform.model.repository;

import com.e.shortform.model.entity.CommunityEntity;
import com.e.shortform.model.entity.CommunityLikeEntity;
import com.e.shortform.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityLikeRepo extends JpaRepository<CommunityLikeEntity, Long> {

    boolean existsByUserAndCommunity(UserEntity user, CommunityEntity community);
    Optional<CommunityLikeEntity> findByUserAndCommunity(UserEntity user, CommunityEntity community);
    long countByCommunity(CommunityEntity community);

}

package com.e.shortform.domain.community.repository;

import com.e.shortform.domain.community.entity.CommunityCommentEntity;
import com.e.shortform.domain.community.entity.CommunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityCommentRepo extends JpaRepository<CommunityCommentEntity, Long> {

    List<CommunityCommentEntity> findByCommunity(CommunityEntity community);

}

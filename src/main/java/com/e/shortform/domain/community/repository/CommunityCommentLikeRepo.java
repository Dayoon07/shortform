package com.e.shortform.domain.community.repository;

import com.e.shortform.domain.community.entity.CommunityCommentLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityCommentLikeRepo extends JpaRepository<CommunityCommentLikeEntity, Long> {
}

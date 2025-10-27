package com.e.shortform.model.repository;

import com.e.shortform.model.entity.CommunityCommentLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityCommentLikeRepo extends JpaRepository<CommunityCommentLikeEntity, Long> {
}

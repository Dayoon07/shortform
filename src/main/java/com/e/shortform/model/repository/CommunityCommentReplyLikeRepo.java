package com.e.shortform.model.repository;

import com.e.shortform.model.entity.CommunityCommentReplyLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityCommentReplyLikeRepo extends JpaRepository<CommunityCommentReplyLikeEntity, Long> {
}

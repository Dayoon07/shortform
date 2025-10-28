package com.e.shortform.domain.community.repository;

import com.e.shortform.domain.community.entity.CommunityCommentReplyLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityCommentReplyLikeRepo extends JpaRepository<CommunityCommentReplyLikeEntity, Long> {
}

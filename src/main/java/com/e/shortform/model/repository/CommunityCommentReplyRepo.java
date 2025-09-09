package com.e.shortform.model.repository;

import com.e.shortform.model.entity.CommunityCommentReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityCommentReplyRepo extends JpaRepository<CommunityCommentReplyEntity, Long> {
}

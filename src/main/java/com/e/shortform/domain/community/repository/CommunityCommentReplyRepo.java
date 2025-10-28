package com.e.shortform.domain.community.repository;

import com.e.shortform.domain.community.entity.CommunityCommentReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityCommentReplyRepo extends JpaRepository<CommunityCommentReplyEntity, Long> {
}

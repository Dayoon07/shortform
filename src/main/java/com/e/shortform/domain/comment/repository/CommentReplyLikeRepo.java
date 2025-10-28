package com.e.shortform.domain.comment.repository;

import com.e.shortform.domain.comment.entity.CommentReplyLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentReplyLikeRepo extends JpaRepository<CommentReplyLikeEntity, Long> {
}

package com.e.shortform.model.repository;

import com.e.shortform.model.entity.CommentReplyLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentReplyLikeRepo extends JpaRepository<CommentReplyLikeEntity, Long> {
}

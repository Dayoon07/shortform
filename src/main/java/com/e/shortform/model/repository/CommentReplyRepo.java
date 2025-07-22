package com.e.shortform.model.repository;

import com.e.shortform.model.entity.CommentReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentReplyRepo extends JpaRepository<CommentReplyEntity, Long> {
}

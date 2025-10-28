package com.e.shortform.domain.comment.repository;

import com.e.shortform.domain.comment.entity.CommentEntity;
import com.e.shortform.domain.comment.entity.CommentReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentReplyRepo extends JpaRepository<CommentReplyEntity, Long> {

    List<CommentReplyEntity> findByParentComment(CommentEntity parentComment);

}

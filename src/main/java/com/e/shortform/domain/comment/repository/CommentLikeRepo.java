package com.e.shortform.domain.comment.repository;

import com.e.shortform.domain.comment.entity.CommentEntity;
import com.e.shortform.domain.comment.entity.CommentLikeEntity;
import com.e.shortform.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepo extends JpaRepository<CommentLikeEntity, Long> {
    Optional<CommentLikeEntity> findByUserAndComment(UserEntity user, CommentEntity comment);
}

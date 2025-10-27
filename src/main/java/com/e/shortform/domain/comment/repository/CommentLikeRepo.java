package com.e.shortform.model.repository;

import com.e.shortform.model.entity.CommentEntity;
import com.e.shortform.model.entity.CommentLikeEntity;
import com.e.shortform.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentLikeRepo extends JpaRepository<CommentLikeEntity, Long> {
    Optional<CommentLikeEntity> findByUserAndComment(UserEntity user, CommentEntity comment);
}

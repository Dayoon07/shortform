package com.e.shortform.model.repository;

import com.e.shortform.model.entity.CommentLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepo extends JpaRepository<CommentLikeEntity, Long> {
}

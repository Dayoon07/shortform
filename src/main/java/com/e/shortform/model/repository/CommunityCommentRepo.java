package com.e.shortform.model.repository;

import com.e.shortform.model.entity.CommunityCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityCommentRepo extends JpaRepository<CommunityCommentEntity, Long> {
}

package com.e.shortform.model.repository;

import com.e.shortform.model.entity.VideoLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoLikeRepo extends JpaRepository<VideoLikeEntity, Long> {
}

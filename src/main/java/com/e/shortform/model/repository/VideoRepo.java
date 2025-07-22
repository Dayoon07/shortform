package com.e.shortform.model.repository;

import com.e.shortform.model.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepo extends JpaRepository<VideoEntity, Long> {
}

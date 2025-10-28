package com.e.shortform.domain.video.repository;


import com.e.shortform.domain.video.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepo extends JpaRepository<VideoEntity, Long> {
    VideoEntity findByVideoLoc(String videoLoc);
}

package com.e.shortform.model.repository;

import com.e.shortform.model.entity.UserEntity;
import com.e.shortform.model.entity.VideoEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepo extends JpaRepository<VideoEntity, Long> {
    VideoEntity findByVideoLoc(String videoLoc);
}

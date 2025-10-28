package com.e.shortform.domain.comment.repository;

import com.e.shortform.domain.comment.entity.CommentEntity;
import com.e.shortform.domain.video.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepo extends JpaRepository<CommentEntity, Long> {

    long countByVideo(VideoEntity video);

}

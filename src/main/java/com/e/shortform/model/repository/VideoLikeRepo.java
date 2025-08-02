package com.e.shortform.model.repository;

import com.e.shortform.model.entity.VideoEntity;
import com.e.shortform.model.entity.UserEntity;
import com.e.shortform.model.entity.VideoLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoLikeRepo extends JpaRepository<VideoLikeEntity, Long> {

    // 특정 유저가 특정 비디오에 좋아요를 눌렀는지 확인
    Optional<VideoLikeEntity> findByVideoAndUser(VideoEntity video, UserEntity user);

    // 특정 유저가 특정 비디오에 좋아요를 눌렀는지 존재 여부만 확인
    boolean existsByVideoAndUser(VideoEntity video, UserEntity user);

    // 특정 비디오의 총 좋아요 수
    long countByVideo(VideoEntity video);

    // 특정 유저의 총 좋아요 수 (받은 좋아요)
    @Query("SELECT COUNT(vl) FROM VideoLikeEntity vl JOIN VideoEntity v ON vl.video.id = v.id WHERE v.uploader.id = :userId")
    long countLikesByUploaderId(@Param("userId") Long userId);

    // 특정 유저가 누른 좋아요 수 (누른 좋아요)
    long countByUser(UserEntity user);

    // 특정 유저가 특정 비디오에 누른 좋아요 삭제
    void deleteByVideoAndUser(VideoEntity video, UserEntity user);
}

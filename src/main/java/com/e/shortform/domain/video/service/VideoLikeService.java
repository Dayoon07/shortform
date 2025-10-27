package com.e.shortform.model.service;

import com.e.shortform.model.entity.UserEntity;
import com.e.shortform.model.entity.VideoEntity;
import com.e.shortform.model.entity.VideoLikeEntity;
import com.e.shortform.model.mapper.VideoLikeMapper;
import com.e.shortform.model.repository.UserRepo;
import com.e.shortform.model.repository.VideoLikeRepo;
import com.e.shortform.model.repository.VideoRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoLikeService {

    private final VideoLikeRepo videoLikeRepo;
    private final VideoLikeMapper videoLikeMapper;
    private final VideoRepo videoRepo;
    private final UserRepo userRepo;

    /**
     * 좋아요 토글 (MyBatis 방식) - 더 빠른 성능
     */
    public LikeToggleResult toggleLikeWithMyBatis(Long videoId, Long userId) {
        try {
            boolean wasLiked = videoLikeMapper.existsLike(videoId, userId) > 0;

            int result;
            if (wasLiked) {
                // 좋아요 취소
                result = videoLikeMapper.deleteLike(videoId, userId);
            } else {
                // 좋아요 추가
                result = videoLikeMapper.insertLike(videoId, userId);
            }

            if (result == 0) {
                throw new RuntimeException("좋아요 처리에 실패했습니다");
            }

            // 현재 총 좋아요 수 조회
            int totalLikes = videoLikeMapper.countLikesByVideoId(videoId);

            return LikeToggleResult.builder()
                    .success(true)
                    .isLiked(!wasLiked)
                    .totalLikes(totalLikes)
                    .message(!wasLiked ? "좋아요를 눌렀습니다" : "좋아요를 취소했습니다")
                    .build();

        } catch (Exception e) {
            log.error("MyBatis 좋아요 토글 처리 중 오류 발생: videoId={}, userId={}", videoId, userId, e);
            return LikeToggleResult.builder()
                    .success(false)
                    .message("좋아요 처리 중 오류가 발생했습니다")
                    .build();
        }
    }

    /**
     * 비디오 좋아요 정보 조회 (JPA 방식)
     */
    @Transactional(readOnly = true)
    public VideoLikeInfo getVideoLikeInfoWithJPA(Long videoId, Long userId) {
        VideoEntity video = videoRepo.findById(videoId).orElse(null);
        long likeCount = videoLikeRepo.countByVideo(video);
        boolean isLiked = userId != null ? videoLikeRepo.existsByVideoAndUser(video, userRepo.findById(userId).get()) : false;

        return VideoLikeInfo.builder()
                .videoId(videoId)
                .likeCount((int) likeCount)
                .isLiked(isLiked)
                .build();
    }

    /**
     * 비디오 좋아요 정보 조회 (MyBatis 방식) - 한 번의 쿼리로 처리
     */
    @Transactional(readOnly = true)
    public VideoLikeInfo getVideoLikeInfoWithMyBatis(Long videoId, Long userId) {
        if (userId != null) {
            return VideoLikeInfo.fromMapperResult(videoLikeMapper.getVideoLikeInfo(videoId, userId));
        } else {
            int likeCount = videoLikeMapper.countLikesByVideoId(videoId);
            return VideoLikeInfo.builder()
                    .videoId(videoId)
                    .likeCount(likeCount)
                    .isLiked(false)
                    .build();
        }
    }

    // DTO 클래스들
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LikeToggleResult {
        private boolean success;
        private boolean isLiked;
        private int totalLikes;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VideoLikeInfo {
        private Long videoId;
        private int likeCount;
        private boolean isLiked;

        public static VideoLikeInfo fromMapperResult(VideoLikeMapper.VideoLikeInfo mapperResult) {
            return VideoLikeInfo.builder()
                    .videoId(mapperResult.getVideoId())
                    .likeCount(mapperResult.getLikeCount())
                    .isLiked(mapperResult.isLiked())
                    .build();
        }
    }

    public long countByVideo(VideoEntity video) {
        return videoLikeRepo.countByVideo(video);
    }

    public boolean existsByVideoAndUser(VideoEntity video, UserEntity user) {
        return videoLikeRepo.existsByVideoAndUser(video, user);
    }

}

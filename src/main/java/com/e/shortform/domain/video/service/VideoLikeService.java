package com.e.shortform.domain.video.service;

import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.repository.UserRepo;
import com.e.shortform.domain.video.entity.VideoEntity;
import com.e.shortform.domain.video.mapper.VideoLikeMapper;
import com.e.shortform.domain.video.repository.VideoLikeRepo;
import com.e.shortform.domain.video.repository.VideoRepo;
import com.e.shortform.domain.video.res.VideoLikeInfoDto;
import com.e.shortform.domain.video.res.VideoLikeToggleDto;
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

    /** 좋아요 토글 (MyBatis 방식) - 더 빠른 성능 */
    public VideoLikeToggleDto toggleLikeWithMyBatis(Long videoId, Long userId) {
        try {
            boolean wasLiked = videoLikeMapper.existsLike(videoId, userId) > 0;
            int result;

            if (wasLiked) { // 좋아요 취소
                result = videoLikeMapper.deleteLike(videoId, userId);
            } else {    // 좋아요 추가
                result = videoLikeMapper.insertLike(videoId, userId);
            }

            if (result == 0) throw new RuntimeException("좋아요 처리에 실패했습니다");
            int totalLikes = videoLikeMapper.countLikesByVideoId(videoId);  // 현재 총 좋아요 수 조회

            return VideoLikeToggleDto.builder()
                    .success(true)
                    .isLiked(!wasLiked)
                    .totalLikes(totalLikes)
                    .message(!wasLiked ? "좋아요를 눌렀습니다" : "좋아요를 취소했습니다")
                    .build();

        } catch (Exception e) {
            log.error("MyBatis 좋아요 토글 처리 중 오류 발생: videoId={}, userId={}", videoId, userId, e);
            return VideoLikeToggleDto.builder()
                    .success(false)
                    .message("좋아요 처리 중 오류가 발생했습니다")
                    .build();
        }
    }

    /** 비디오 좋아요 정보 조회 (JPA 방식) */
    @Transactional(readOnly = true)
    public VideoLikeInfoDto getVideoLikeInfoWithJPA(Long videoId, Long userId) {
        VideoEntity video = videoRepo.findById(videoId).orElse(null);
        long likeCount = videoLikeRepo.countByVideo(video);
        boolean isLiked = userId != null && videoLikeRepo.existsByVideoAndUser(video, userRepo.findById(userId).orElseThrow());

        return VideoLikeInfoDto.builder()
                .videoId(videoId)
                .likeCount((int) likeCount)
                .isLiked(isLiked)
                .build();
    }

    /** 비디오 좋아요 정보 조회 (MyBatis 방식) - 한 번의 쿼리로 처리 */
    @Transactional(readOnly = true)
    public VideoLikeInfoDto getVideoLikeInfoWithMyBatis(Long videoId, Long userId) {
        if (userId != null) {
            return VideoLikeInfoDto.fromMapperResult(videoLikeMapper.getVideoLikeInfo(videoId, userId));
        } else {
            int likeCount = videoLikeMapper.countLikesByVideoId(videoId);
            return VideoLikeInfoDto.builder()
                    .videoId(videoId)
                    .likeCount(likeCount)
                    .isLiked(false)
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

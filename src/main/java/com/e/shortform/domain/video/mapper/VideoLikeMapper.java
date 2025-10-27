package com.e.shortform.model.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface VideoLikeMapper {

    // 좋아요 추가
    @Insert("INSERT INTO SHORTFORM_VIDEO_LIKES (LIKE_VIDEO_ID, LIKER_USER_ID) VALUES (#{videoId}, #{userId})")
    int insertLike(@Param("videoId") Long videoId, @Param("userId") Long userId);

    // 좋아요 삭제
    @Delete("DELETE FROM SHORTFORM_VIDEO_LIKES WHERE LIKE_VIDEO_ID = #{videoId} AND LIKER_USER_ID = #{userId}")
    int deleteLike(@Param("videoId") Long videoId, @Param("userId") Long userId);

    // 좋아요 존재 여부 확인
    @Select("SELECT COUNT(*) FROM SHORTFORM_VIDEO_LIKES WHERE LIKE_VIDEO_ID = #{videoId} AND LIKER_USER_ID = #{userId}")
    int existsLike(@Param("videoId") Long videoId, @Param("userId") Long userId);

    // 특정 비디오의 총 좋아요 수
    @Select("SELECT COUNT(*) FROM SHORTFORM_VIDEO_LIKES WHERE LIKE_VIDEO_ID = #{videoId}")
    int countLikesByVideoId(@Param("videoId") Long videoId);

    // 특정 유저가 받은 총 좋아요 수 (업로드한 비디오들의 좋아요 합계)
    @Select("""
        SELECT COUNT(*) 
        FROM SHORTFORM_VIDEO_LIKES vl 
        JOIN SHORTFORM_VIDEOS v ON vl.LIKE_VIDEO_ID = v.ID 
        WHERE v.UPLOADER_USER_ID = #{userId}
    """)
    int countReceivedLikesByUserId(@Param("userId") Long userId);

    // 특정 유저가 누른 총 좋아요 수
    @Select("SELECT COUNT(*) FROM SHORTFORM_VIDEO_LIKES WHERE LIKER_USER_ID = #{userId}")
    int countGivenLikesByUserId(@Param("userId") Long userId);

    // 비디오별 좋아요 수와 현재 유저의 좋아요 상태를 함께 조회 (복잡한 쿼리용)
    @Select("""
        SELECT 
            v.ID as videoId,
            COUNT(vl.ID) as likeCount,
            CASE WHEN user_like.ID IS NOT NULL THEN 1 ELSE 0 END as isLiked
        FROM SHORTFORM_VIDEOS v
        LEFT JOIN SHORTFORM_VIDEO_LIKES vl ON v.ID = vl.LIKE_VIDEO_ID
        LEFT JOIN SHORTFORM_VIDEO_LIKES user_like ON v.ID = user_like.LIKE_VIDEO_ID AND user_like.LIKER_USER_ID = #{userId}
        WHERE v.ID = #{videoId}
        GROUP BY v.ID, user_like.ID
    """)
    @Results({
            @Result(property = "videoId", column = "videoId"),
            @Result(property = "likeCount", column = "likeCount"),
            @Result(property = "isLiked", column = "isLiked")
    })
    VideoLikeInfo getVideoLikeInfo(@Param("videoId") Long videoId, @Param("userId") Long userId);

    // DTO 클래스
    class VideoLikeInfo {
        private Long videoId;
        private int likeCount;
        private boolean isLiked;

        // getters, setters, constructors
        public VideoLikeInfo() {}

        public VideoLikeInfo(Long videoId, int likeCount, boolean isLiked) {
            this.videoId = videoId;
            this.likeCount = likeCount;
            this.isLiked = isLiked;
        }

        public Long getVideoId() { return videoId; }
        public void setVideoId(Long videoId) { this.videoId = videoId; }

        public int getLikeCount() { return likeCount; }
        public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

        public boolean isLiked() { return isLiked; }
        public void setLiked(boolean liked) { isLiked = liked; }
    }

}

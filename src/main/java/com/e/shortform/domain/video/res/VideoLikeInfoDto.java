package com.e.shortform.domain.video.res;

import com.e.shortform.domain.video.mapper.VideoLikeMapper;
import com.e.shortform.domain.video.service.VideoLikeService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoLikeInfoDto {

    private Long videoId;
    private int likeCount;
    private boolean isLiked;

    public static VideoLikeInfoDto fromMapperResult(VideoLikeMapper.VideoLikeInfo mapperResult) {
        return VideoLikeInfoDto.builder()
                .videoId(mapperResult.getVideoId())
                .likeCount(mapperResult.getLikeCount())
                .isLiked(mapperResult.isLiked())
                .build();
    }

}

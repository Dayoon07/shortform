package com.e.shortform.domain.video.res;

import com.e.shortform.domain.user.vo.UserVo;
import com.e.shortform.domain.video.vo.VideoVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoLikeWithUserVideoDto {

    private Long id;
    private Long likeVideoId;
    private Long likerUserId;
    private LocalDateTime likeAt;

    private UserVo userVo;
    private VideoVo videoVo;

}

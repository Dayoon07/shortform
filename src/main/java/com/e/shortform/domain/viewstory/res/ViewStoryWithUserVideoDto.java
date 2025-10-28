package com.e.shortform.domain.viewstory.res;

import com.e.shortform.domain.user.vo.UserVo;
import com.e.shortform.domain.video.vo.VideoVo;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewStoryWithUserVideoDto {

    private Long id;
    private Long watchedVideoId;
    private Long watchedUserId;
    private LocalDateTime createAt;

    private UserVo userVo;
    private VideoVo videoVo;

}

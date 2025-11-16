package com.e.shortform.domain.video.res;

import com.e.shortform.domain.user.vo.UserVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoWithUserDto {

    private Long id;
    private String videoTitle;
    private String videoDescription;
    private String videoName;
    private String videoSrc;
    private String videoTag;
    private Long videoViews;
    private String videoLoc;
    private Long uploaderUserId;
    private String videoWatchAvailability;
    private String commentAvailability;
    private String previewImg;
    private LocalDateTime uploadAt;

    private UserVo userVo;

}

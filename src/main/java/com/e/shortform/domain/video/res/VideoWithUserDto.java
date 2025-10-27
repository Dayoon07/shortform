package com.e.shortform.model.dto;

import com.e.shortform.model.vo.UserVo;
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
    private LocalDateTime uploadAt;

    private UserVo userVo;

}

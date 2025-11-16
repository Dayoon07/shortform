package com.e.shortform.domain.video.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoWithUserFuckingDto {

    private Long videoId;
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
    private Long userId;
    private String username;
    private String mail;
    private String profileImg;
    private String profileImgSrc;
    private String previewImg;
    private String bio;
    private String mention;
    private LocalDateTime createAt;

}

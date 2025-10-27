package com.e.shortform.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexPageAllVideosDto {

    private Long videoId;
    private String videoTitle;
    private String videoDescription;
    private String videoName;
    private String videoSrc;
    private String videoTag;
    private Long videoViews;
    private String videoLoc;
    private String videoWatchAvailability;
    private String commentAvailability;
    private LocalDateTime uploadAt;
    private Long uploaderId;
    private String uploaderUsername;
    private String uploaderMail;
    private String profileImg;
    private String profileImgSrc;
    private String bio;
    private String mention;
    private LocalDateTime uploaderCreateAt;
    private Long likeCount;

}

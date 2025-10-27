package com.e.shortform.model.vo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoVo {

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

}

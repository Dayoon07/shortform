package com.e.shortform.domain.video.res;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponseDto {
    private Long id;
    private String videoTitle;
    private String videoSrc;
    private String videoDescription;
    private String videoTag;
    private LocalDateTime uploadAt;
}
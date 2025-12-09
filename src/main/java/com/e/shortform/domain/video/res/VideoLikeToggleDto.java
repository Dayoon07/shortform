package com.e.shortform.domain.video.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoLikeToggleDto {

    private boolean success;
    private boolean isLiked;
    private int totalLikes;
    private String message;

}

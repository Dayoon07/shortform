package com.e.shortform.model.vo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoLikeVo {

    private Long id;
    private Long likeVideoId;
    private Long likerUserId;
    private LocalDateTime likeAt;

}

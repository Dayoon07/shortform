package com.e.shortform.domain.viewstory.vo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewStoryVo {

    private Long id;
    private Long watchedVideoId;
    private Long watchedUserId;
    private LocalDateTime createAt;

}

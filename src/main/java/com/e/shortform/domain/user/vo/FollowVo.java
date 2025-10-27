package com.e.shortform.model.vo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowVo {

    private Long id;
    private Long followUserId;
    private Long followedUserId;
    private LocalDateTime createAt;

}

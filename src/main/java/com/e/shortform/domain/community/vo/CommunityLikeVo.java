package com.e.shortform.domain.community.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityLikeVo {

    private Long id;
    private Long communityId;
    private Long likerUserId;
    private LocalDateTime likeAt;

}

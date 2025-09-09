package com.e.shortform.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityCommentLikeVo {

    private Long id;
    private Long likerUserId;
    private Long commentId;
    private LocalDateTime likeAt;

}

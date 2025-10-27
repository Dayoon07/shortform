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
public class CommunityCommentReplyLikeVo {

    private Long id;
    private Long replyId;
    private Long likerUserId;
    private LocalDateTime likeAt;

}

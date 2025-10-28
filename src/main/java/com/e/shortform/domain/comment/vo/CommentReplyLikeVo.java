package com.e.shortform.domain.comment.vo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReplyLikeVo {

    private Long id;
    private Long replyId;
    private Long likerUserId;
    private LocalDateTime likeAt;

}

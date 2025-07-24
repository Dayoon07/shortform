package com.e.shortform.model.vo;

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

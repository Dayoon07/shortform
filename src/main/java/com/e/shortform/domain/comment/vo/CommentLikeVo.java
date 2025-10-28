package com.e.shortform.domain.comment.vo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentLikeVo {

    private Long id;
    private Long commentId;
    private Long likerUserId;
    private LocalDateTime likeAt;

}

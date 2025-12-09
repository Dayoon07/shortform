package com.e.shortform.domain.comment.req;

import lombok.Data;

@Data
public class CommentReplyReqDto {
    private Long commentReplyId;
    private String commentReplyText;
}

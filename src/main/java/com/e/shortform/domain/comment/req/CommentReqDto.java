package com.e.shortform.domain.comment.req;

import lombok.Data;

@Data
public class CommentReqDto {
    private String commentText;
    private Long commentVideoId;
}

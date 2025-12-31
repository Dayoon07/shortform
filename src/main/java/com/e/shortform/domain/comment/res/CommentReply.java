package com.e.shortform.domain.comment.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReply {

    private Long id;
    private String commentReplyText;
    private Long commentReplyUserId;
    private Long commentReplyId;
    private String createAt;
    private String username;
    private String mention;
    private String profileImgSrc;
    private boolean social;
    private String provider;
    private Long likeCount;

}

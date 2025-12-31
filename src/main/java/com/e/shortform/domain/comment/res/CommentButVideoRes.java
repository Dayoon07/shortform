package com.e.shortform.domain.comment.res;

import com.e.shortform.domain.user.vo.UserVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentButVideoRes {

    private Long id;
    private String commentText;
    private Long commentUserId;
    private Long commentVideoId;
    private String createAt;

    private String username;
    private String mention;
    private String profileImgSrc;
    private boolean social;
    private String provider;

    private Long likeCount;
    private Long replyCount;

}

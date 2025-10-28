package com.e.shortform.domain.comment.res;

import com.e.shortform.domain.comment.vo.CommentVo;
import com.e.shortform.domain.user.vo.UserVo;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReplyWithUserCommentDto {

    private Long id;
    private String commentReplyText;
    private Long commentReplyUserId;
    private Long commentReplyId;
    private LocalDateTime createAt;

    private UserVo userVo;
    private CommentVo commentVo;

}

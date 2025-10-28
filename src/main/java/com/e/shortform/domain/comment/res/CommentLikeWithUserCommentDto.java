package com.e.shortform.domain.comment.res;

import com.e.shortform.domain.comment.vo.CommentVo;
import com.e.shortform.domain.user.vo.UserVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentLikeWithUserCommentDto {

    private Long id;
    private Long commentId;
    private Long likerUserId;
    private LocalDateTime likeAt;

    private UserVo userVo;
    private CommentVo commentVo;

}

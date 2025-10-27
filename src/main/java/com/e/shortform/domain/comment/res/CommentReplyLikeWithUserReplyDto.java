package com.e.shortform.model.dto;

import com.e.shortform.model.vo.CommentReplyVo;
import com.e.shortform.model.vo.UserVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReplyLikeWithUserReplyDto {

    private Long id;
    private Long replyId;
    private Long likerUserId;
    private LocalDateTime likeAt;

    private UserVo userVo;
    private CommentReplyVo commentReplyVo;

}

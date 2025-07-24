package com.e.shortform.model.dto;

import com.e.shortform.model.vo.CommentVo;
import com.e.shortform.model.vo.UserVo;
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

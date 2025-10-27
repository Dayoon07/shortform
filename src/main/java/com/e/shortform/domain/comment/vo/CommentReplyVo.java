package com.e.shortform.model.vo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReplyVo {

    private Long id;
    private String commentReplyText;
    private Long commentReplyUserId;
    private Long commentReplyId;
    private LocalDateTime createAt;

}

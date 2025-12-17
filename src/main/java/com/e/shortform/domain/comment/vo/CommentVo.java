package com.e.shortform.domain.comment.vo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentVo {

    private Long id;
    private String commentText;
    private Long commentUserId;
    private Long commentVideoId;
    private LocalDateTime createAt;
    private boolean deleteStatus;

}

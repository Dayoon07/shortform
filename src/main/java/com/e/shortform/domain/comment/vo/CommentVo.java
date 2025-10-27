package com.e.shortform.model.vo;

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

}

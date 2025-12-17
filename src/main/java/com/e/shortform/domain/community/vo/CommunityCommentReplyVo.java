package com.e.shortform.domain.community.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityCommentReplyVo {

    public Long id;
    public String replyText;
    private Long replyUserId;
    private Long commentId;
    private LocalDateTime createAt;
    private boolean deleteStatus;

}

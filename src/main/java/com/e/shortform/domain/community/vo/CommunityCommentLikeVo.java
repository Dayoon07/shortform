package com.e.shortform.domain.community.vo;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityCommentLikeVo {

    private Long id;
    private Long likerUserId;
    private Long commentId;
    private LocalDateTime likeAt;

}

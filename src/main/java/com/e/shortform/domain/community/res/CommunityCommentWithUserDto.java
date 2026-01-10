package com.e.shortform.domain.community.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityCommentWithUserDto {

    private Long id;
    private String commentText;
    private Long commentUserId;
    private Long communityId;
    private LocalDateTime createAt;
    private String username;
    private String profileImgSrc;
    private String mention;
    private boolean social;
    private String provider;
    private Long likeCount;
    private Long replyCount;

}
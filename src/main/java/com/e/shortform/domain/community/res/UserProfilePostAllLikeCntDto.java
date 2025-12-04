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
public class UserProfilePostAllLikeCntDto {

    private Long id;
    private String username;
    private String profileImgSrc;
    private String mention;
    private boolean social;
    private String provider;
    private String communityUuid;
    private Long communityWriterId;
    private String communityText;
    private LocalDateTime createAt;
    private String files;
    private Long likeCnt;
    private Long commentCnt;

}

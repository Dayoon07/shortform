package com.e.shortform.domain.user.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {

    private Long id;
    private String username;
    private String mail;
    private String profileImg;
    private String profileImgSrc;
    private String bio;
    private String mention;
    private Timestamp createAt;

    private Long followerCount;
    private Long followingCount;
    private Long totalViews;
    private Long totalLikes;
}

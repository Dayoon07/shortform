package com.e.shortform.domain.user.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 팔로우 토글 결과를 담는 클래스 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowToggleDto {

    private boolean isFollowing;
    private boolean success;
    private String message;

}
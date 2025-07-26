package com.e.shortform.model.dto;

import com.e.shortform.model.vo.UserVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowWithUserDto {

    private Long id;
    private Long followUserId;
    private Long followedUserId;
    private LocalDateTime createAt;

    private UserVo followUser;
    private UserVo followedUser;

}

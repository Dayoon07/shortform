package com.e.shortform.domain.user.vo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVo {

    private Long id;
    private String username;
    private String mail;
    private String password;
    private String profileImg;
    private String profileImgSrc;
    private String bio;
    private String mention;
    private LocalDateTime createAt;

}

package com.e.shortform.domain.user.req;

import lombok.Data;

@Data
public class AuthUserReqDto {

    private Long id;
    private String username;
    private String mail;
    private String mention;

}

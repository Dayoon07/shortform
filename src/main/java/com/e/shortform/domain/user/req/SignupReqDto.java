package com.e.shortform.domain.user.req;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class SignupReqDto {

    private String email;
    private String username;
    private String password;
    private MultipartFile profileImage;

}

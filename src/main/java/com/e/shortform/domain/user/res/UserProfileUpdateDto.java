package com.e.shortform.domain.user.res;

import lombok.Data;

@Data
public class UserProfileUpdateDto {
    private String username;
    private String mail;
    private String mention;
    private String bio;
    // 이 필드는 더 이상 사용하지 않거나, 이미지 삭제 플래그 등으로 활용해야 합니다.
    // 서버에서 파일 경로를 생성하여 이 DTO에 다시 담는 로직은 제거됩니다.
    // private String profileImg;
    // private String profileImgSrc;
}

package com.e.shortform.domain.user.res;

public record UserProfileUpdateDto(
        Long id,
        String username,
        String mail,
        String mention,
        String bio,
        String profileImg,
        String profileImgSrc
) {}


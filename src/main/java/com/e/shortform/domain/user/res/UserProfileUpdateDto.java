package com.e.shortform.model.dto;

public record UserProfileUpdateDto(
        Long id,
        String username,
        String mail,
        String mention,
        String bio,
        String profileImg,
        String profileImgSrc
) {}


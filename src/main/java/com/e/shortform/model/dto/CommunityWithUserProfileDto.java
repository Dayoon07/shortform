package com.e.shortform.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityWithUserProfileDto {

    private Long id;
    private String username;
    private String profileImgSrc;
    private String mention;
    private String communityUuid;
    private Long communityWriterId;
    private String communityText;
    private LocalDateTime createAt;
    private String files;

}

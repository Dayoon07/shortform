package com.e.shortform.domain.user.req;

import lombok.Data;

@Data
public class FollowToggleReqDto {

    private String reqMention;
    private String resMention;

}

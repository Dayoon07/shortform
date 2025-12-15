package com.e.shortform.domain.report.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TargetType {

    VIDEO("VIDEO"),
    COMMENT("COMMENT"),
    COMMENT_REPLY("COMMENT_REPLY"),
    COMMUNITY("COMMUNITY"),
    COMMUNITY_COMMENT("COMMUNITY_COMMENT"),
    COMMUNITY_COMMENT_REPLY("COMMUNITY_COMMENT_REPLY");

    private final String value;
    
}
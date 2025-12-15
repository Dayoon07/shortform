package com.e.shortform.domain.report.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActionTakenType {

    NONE("NONE"),
    WARNING("WARNING"),
    CONTENT_HIDDEN("CONTENT_HIDDEN"),
    CONTENT_DELETED("CONTENT_DELETED"),
    USER_SUSPENDED("USER_SUSPENDED"),
    USER_BANNED("USER_BANNED");

    private final String value;

}

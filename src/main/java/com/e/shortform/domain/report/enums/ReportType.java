package com.e.shortform.domain.report.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportType {

    INAPPROPRIATE("INAPPROPRIATE"),
    SEXUAL("SEXUAL"),
    VIOLENCE("VIOLENCE"),
    HATE_SPEECH("HATE_SPEECH"),
    SPAM("SPAM"),
    COPYRIGHT("COPYRIGHT"),
    FALSE_INFO("FALSE_INFO"),
    PRIVACY("PRIVACY"),
    ILLEGAL("ILLEGAL"),
    ETC("ETC");

    private final String value;

}
